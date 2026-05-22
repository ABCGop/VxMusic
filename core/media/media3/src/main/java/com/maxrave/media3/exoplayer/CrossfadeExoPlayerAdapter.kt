package com.maxrave.media3.exoplayer

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.SilenceSkippingAudioProcessor
import com.maxrave.domain.data.player.GenericMediaItem
import com.maxrave.domain.data.player.GenericPlaybackParameters
import com.maxrave.domain.data.player.PlayerConstants
import com.maxrave.domain.data.player.PlayerError
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.mediaservice.player.MediaPlayerInterface
import com.maxrave.domain.mediaservice.player.MediaPlayerListener
import com.maxrave.domain.repository.StreamRepository
import com.maxrave.logger.Logger
import com.maxrave.media3.audio.BiquadFilter
import com.maxrave.media3.audio.CrossfadeFilterAudioProcessor
import com.maxrave.media3.exoplayer.CrossfadeExoPlayerAdapter.Companion.AUTO_FALLBACK_DURATION_MS
import com.maxrave.media3.service.mediasourcefactory.MergingMediaSourceFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln

private const val TAG = "CrossfadeExoPlayerAdapter"

/**
 * ExoPlayer implementation of [MediaPlayerInterface] with crossfade support.
 *
 * Architecture mirrors [com.simpmusic.media_jvm.GstreamerPlayerAdapter]:
 * - Internal playlist management (not ExoPlayer's playlist)
 * - Multi-player instance model: each track gets its own ExoPlayer
 * - Precaching system for smooth transitions
 * - Crossfade with listener swap pattern
 * - [DelegatingForwardingPlayer] for MediaSession integration
 *
 * Key difference from GstreamerPlayerAdapter:
 * - Uses [MergingMediaSourceFactory] + ResolvingDataSource for URL resolution
 *   (instead of manually extracting URLs via StreamRepository)
 * - Each ExoPlayer gets a single [MediaItem] and auto-resolves the stream URL
 */
@SuppressLint("UnsafeOptInUsageError")
@OptIn(UnstableApi::class)
internal class CrossfadeExoPlayerAdapter(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val dataStoreManager: DataStoreManager,
    private val mediaSourceFactory: MergingMediaSourceFactory,
    private val audioAttributes: AudioAttributes,
    private val streamRepository: StreamRepository,
) : MediaPlayerInterface {
    // ========== Internal State Enum (same as GstreamerPlayerAdapter) ==========

    private enum class InternalState {
        IDLE, // No media loaded
        PREPARING, // Loading media
        READY, // Ready to play/paused
        PLAYING, // Currently playing
        PAUSED,
        ENDED, // Playback ended
        ERROR, // Error state
    }

    private fun InternalState.isInReadyState(): Boolean = this == InternalState.READY || this == InternalState.PLAYING || this == InternalState.PAUSED

    // ========== Crossfade Settings (loaded from DataStore) ==========

    init {
        coroutineScope.launch {
            dataStoreManager.crossfadeEnabled.collect { enabled ->
                crossfadeEnabled = (enabled == DataStoreManager.TRUE)
                Logger.d(TAG, "Crossfade enabled: $crossfadeEnabled")
            }
        }
        coroutineScope.launch {
            dataStoreManager.crossfadeDuration.collect { duration ->
                crossfadeDurationMs = duration
                Logger.d(TAG, "Crossfade duration: $crossfadeDurationMs ms")
            }
        }
        coroutineScope.launch {
            dataStoreManager.crossfadeDjMode.collect { enabled ->
                djCrossfadeEnabled = (enabled == DataStoreManager.TRUE)
                Logger.d(TAG, "DJ crossfade mode: $djCrossfadeEnabled")
            }
        }
    }

    // ========== State Management ==========

    private val listeners = mutableListOf<MediaPlayerListener>()

    @Volatile
    private var currentPlayer: ExoPlayer? = null

    @Volatile
    private var internalState = InternalState.IDLE

    @Volatile
    private var internalPlayWhenReady = true

    @Volatile
    private var internalVolume = 1.0f

    @Volatile
    private var internalRepeatMode = PlayerConstants.REPEAT_MODE_OFF

    @Volatile
    private var internalShuffleModeEnabled = false

    @Volatile
    private var internalPlaybackSpeed = 1.0f

    @Volatile
    private var internalPlaybackPitch = 1.0f

    @Volatile
    private var internalSkipSilence = false

    // Position tracking - updated periodically, not on every query
    @Volatile
    private var cachedPosition = 0L

    @Volatile
    private var cachedDuration = 0L

    @Volatile
    private var cachedBufferedPosition = 0L

    @Volatile
    private var cachedIsLoading = false

    private var positionUpdateJob: Job? = null

    // Active Player.Listener (equivalent to BusListeners in GstreamerPlayerAdapter)
    // Only ONE listener instance, attached to ONE ExoPlayer at a time.
    // Swapped between players during crossfade.
    private var activePlayerListener: Player.Listener? = null

    // ========== Precaching System ==========

    private data class PrecachedPlayer(
        val player: ExoPlayer,
        val mediaItem: GenericMediaItem,
        val filter: CrossfadeFilterAudioProcessor? = null,
    )

    // VideoId -> PrecachedPlayer
    private val precachedPlayers = ConcurrentHashMap<String, PrecachedPlayer>()
    private var precacheEnabled = true
    private val maxPrecacheCount = 2
    private var precacheJob: Job? = null

    // ========== Crossfade System ==========

    @Volatile
    private var crossfadeEnabled = false

    @Volatile
    private var crossfadeDurationMs = 5000

    @Volatile
    private var djCrossfadeEnabled = true

    @Volatile
    private var secondaryPlayer: ExoPlayer? = null

    @Volatile
    private var crossfadeJob: Job? = null

    @Volatile
    private var isCrossfading = false

    // Per-player filter references for DJ-style crossfade
    @Volatile
    private var currentPlayerFilter: CrossfadeFilterAudioProcessor? = null

    @Volatile
    private var secondaryPlayerFilter: CrossfadeFilterAudioProcessor? = null

    /** Index we're crossfading from; used when cancelling to revert localCurrentMediaItemIndex. */
    @Volatile
    private var crossfadeFromIndex = -1

    // ========== Retry on Source Error ==========
    // Track retry attempts per media item to avoid infinite retry loops
    private var retryCount = 0
    private var retryVideoId: String? = null
    private val maxRetryCount = 2

    // ========== AutoMix Metadata Cache ==========
    // videoId -> audio analysis data from Tidal (populated externally when 320kbps stream is fetched)
    private val audioMetaCache = ConcurrentHashMap<String, SongAudioMeta>()

    /**
     * Update crossfade state and notify listeners when it changes.
     */
    private fun setCrossfading(value: Boolean) {
        if (isCrossfading != value) {
            isCrossfading = value
            listeners.forEach { it.onCrossfadeStateChanged(value) }
        }
    }

    // ========== Playlist Management ==========

    private val playlist = mutableListOf<GenericMediaItem>()
    private var localCurrentMediaItemIndex = -1

    // Shuffle management
    private var shuffleIndices = mutableListOf<Int>()
    private var shuffleOrder = mutableListOf<Int>()

    // Loading management
    private var currentLoadJob: Job? = null

    // ========== ForwardingPlayer for MediaSession ==========

    // Create an initial idle ExoPlayer for MediaSession to hold
    private val initialPlayerWithFilter = createExoPlayerInstance(handleAudioFocus = true)

    /**
     * Stable [Player] reference for MediaSession.
     * Delegates all calls to the currently active [ExoPlayer] instance.
     * Updated via [DelegatingForwardingPlayer.swapDelegate] when the active player changes.
     */
    val forwardingPlayer: DelegatingForwardingPlayer = DelegatingForwardingPlayer(initialPlayerWithFilter.player)

    init {
        currentPlayer = initialPlayerWithFilter.player
        currentPlayerFilter = initialPlayerWithFilter.filter

        // Wire up playlist navigation so ForwardingPlayer (and thus MediaSession)
        // can see the full playlist state instead of the single-item ExoPlayer state.
        // Only navigation commands are overridden — NOT getMediaItemCount/getCurrentMediaItemIndex
        // which must stay consistent with ExoPlayer's internal Timeline to avoid crashes.
        forwardingPlayer.playlistNavigationProvider =
            object : DelegatingForwardingPlayer.PlaylistNavigationProvider {
                override fun hasNextMediaItem(): Boolean = this@CrossfadeExoPlayerAdapter.hasNextMediaItem()

                override fun hasPreviousMediaItem(): Boolean = this@CrossfadeExoPlayerAdapter.hasPreviousMediaItem()

                override fun seekToNext(): Unit = this@CrossfadeExoPlayerAdapter.seekToNext()

                override fun seekToPrevious(): Unit = this@CrossfadeExoPlayerAdapter.seekToPrevious()

                override fun seekToPreviousMediaItem(): Unit =
                    this@CrossfadeExoPlayerAdapter.seekToPreviousMediaItem()
            }
    }

    // ========== ExoPlayer Instance Factory ==========

    /**
     * Result of creating an ExoPlayer instance, bundled with its per-player crossfade filter.
     */
    private data class PlayerWithFilter(
        val player: ExoPlayer,
        val filter: CrossfadeFilterAudioProcessor,
    )

    /**
     * Create a new ExoPlayer instance with a per-player [CrossfadeFilterAudioProcessor].
     *
     * Each player gets its own filter instance so the fade-out player can have
     * an independent low-pass filter while the fade-in player has a high-pass filter.
     *
     * @param handleAudioFocus true for the current playing player, false for precached/secondary
     */
    private fun createExoPlayerInstance(handleAudioFocus: Boolean = false): PlayerWithFilter {
        val crossfadeFilter = CrossfadeFilterAudioProcessor()

        val perPlayerRenderers =
            object : DefaultRenderersFactory(context) {
                override fun buildAudioSink(
                    context: Context,
                    enableFloatOutput: Boolean,
                    enableAudioTrackPlaybackParams: Boolean,
                ): AudioSink =
                    DefaultAudioSink
                        .Builder(context)
                        .setEnableFloatOutput(enableFloatOutput)
                        .setEnableAudioOutputPlaybackParameters(enableAudioTrackPlaybackParams)
                        .setAudioProcessorChain(
                            DefaultAudioSink.DefaultAudioProcessorChain(
                                arrayOf(crossfadeFilter),
                                SilenceSkippingAudioProcessor(
                                    2_000_000,
                                    (20_000 / 2_000_000).toFloat(),
                                    2_000_000,
                                    0,
                                    256,
                                ),
                                SonicAudioProcessor(),
                            ),
                        ).build()
            }

        val player =
            ExoPlayer
                .Builder(context)
                .setAudioAttributes(audioAttributes, handleAudioFocus)
                .setLoadControl(
                    DefaultLoadControl
                        .Builder()
                        .setBufferDurationsMs(
                            DefaultLoadControl.DEFAULT_MIN_BUFFER_MS * 4,
                            DefaultLoadControl.DEFAULT_MAX_BUFFER_MS * 4,
                            0,
                            0,
                        ).build(),
                ).setWakeMode(C.WAKE_MODE_NETWORK)
                .setHandleAudioBecomingNoisy(handleAudioFocus)
                .setSeekForwardIncrementMs(5000)
                .setSeekBackIncrementMs(5000)
                .setMediaSourceFactory(mediaSourceFactory)
                .setRenderersFactory(perPlayerRenderers)
                .build()

        return PlayerWithFilter(player, crossfadeFilter)
    }

    // ========== Playback Control ==========

    override fun play() {
        Logger.d(TAG, "play() called (state: $internalState, playWhenReady: $internalPlayWhenReady)")
        coroutineScope.launch {
            when (internalState) {
                InternalState.READY, InternalState.ENDED, InternalState.PAUSED -> {
                    currentPlayer?.let { player ->
                        player.play()
                        transitionToState(InternalState.PLAYING)
                        internalPlayWhenReady = true
                    } ?: Logger.w(TAG, "Play called but currentPlayer is null")
                }

                InternalState.PREPARING -> {
                    internalPlayWhenReady = true
                    Logger.d(TAG, "Play: During PREPARING - will auto-play when ready")
                }

                InternalState.PLAYING -> {
                    internalPlayWhenReady = true
                    cachedIsLoading = false
                }

                else -> {
                    Logger.w(TAG, "Play: Called in invalid state: $internalState")
                }
            }
        }
    }

    override fun pause() {
        Logger.d(TAG, "pause() called (state: $internalState, playWhenReady: $internalPlayWhenReady)")
        coroutineScope.launch {
            // Cancel any ongoing crossfade
            if (isCrossfading) {
                Logger.d(TAG, "Pause: Cancelling crossfade")
                crossfadeJob?.cancel()
                crossfadeJob = null
                currentPlayerFilter?.enabled = false
                secondaryPlayerFilter?.enabled = false
                setCrossfading(false)
                // Remove listener from secondaryPlayer BEFORE release to prevent STATE_ENDED
                // from triggering handleTrackEndInternal() and skipping to A+2
                cleanupPlayerListenerInternal()
                stopPositionUpdates()
                // Swap delegate back to currentPlayer (was pointing to secondaryPlayer)
                currentPlayer?.let { forwardingPlayer.swapDelegate(it) }
                setupPlayerListenerInternal(currentPlayer!!)
                // Revert index: we're staying on the track currentPlayer was playing (A)
                if (crossfadeFromIndex >= 0) {
                    localCurrentMediaItemIndex = crossfadeFromIndex
                    playlist.getOrNull(crossfadeFromIndex)?.let { mediaItem ->
                        listeners.forEach {
                            it.onMediaItemTransition(
                                mediaItem,
                                PlayerConstants.MEDIA_ITEM_TRANSITION_REASON_SEEK,
                            )
                        }
                    }
                    forwardingPlayer.notifyMediaItemChanged()
                    crossfadeFromIndex = -1
                }
                secondaryPlayer?.release()
                secondaryPlayer = null
                secondaryPlayerFilter = null
            }

            when (internalState) {
                InternalState.PLAYING, InternalState.READY -> {
                    currentPlayer?.let { player ->
                        player.pause()
                        transitionToState(InternalState.PAUSED)
                        internalPlayWhenReady = false
                    }
                }

                InternalState.PREPARING -> {
                    internalPlayWhenReady = false
                }

                else -> {
                    Logger.w(TAG, "Pause: Called in invalid state: $internalState")
                }
            }
        }
    }

    override fun stop() {
        coroutineScope.launch {
            currentPlayer?.let { player ->
                Logger.d(TAG, "Stop called")
                player.stop()
                transitionToState(InternalState.IDLE)
                stopPositionUpdates()
                notifyEqualizerIntent(false)
            }
        }
    }

    override fun seekTo(positionMs: Long) {
        currentPlayer?.let { player ->
            try {
                player.seekTo(positionMs)
                cachedPosition = positionMs
            } catch (e: Exception) {
                Logger.e(TAG, "Seek exception: ${e.message}", e)
            }
        }
    }

    override fun seekTo(
        mediaItemIndex: Int,
        positionMs: Long,
    ) {
        if (mediaItemIndex !in playlist.indices) return

        coroutineScope.launch {
            val shouldPlay = internalPlayWhenReady

            // Cancel any ongoing crossfade
            if (isCrossfading) {
                Logger.d(TAG, "seekTo: Cancelling crossfade")
                crossfadeJob?.cancel()
                crossfadeJob = null
                currentPlayerFilter?.enabled = false
                secondaryPlayerFilter?.enabled = false
                secondaryPlayer?.release()
                secondaryPlayer = null
                secondaryPlayerFilter = null
                setCrossfading(false)
            }

            // Cancel any ongoing load
            currentLoadJob?.cancel()

            // Load the new track
            localCurrentMediaItemIndex = mediaItemIndex
            loadAndPlayTrackInternal(mediaItemIndex, positionMs, shouldPlay)
        }
    }

    override fun seekBack() {
        val newPosition = (cachedPosition - 5000).coerceAtLeast(0)
        seekTo(newPosition)
    }

    override fun seekForward() {
        val newPosition = (cachedPosition + 5000).coerceAtMost(cachedDuration)
        seekTo(newPosition)
    }

    override fun seekToNext() {
        if (hasNextMediaItem()) {
            // During crossfade A→A+1: user pressing "next" means go to the track we're fading in (A+1).
            // localCurrentMediaItemIndex was already updated to A+1 in triggerCrossfadeTransition,
            // so getNextMediaItemIndex() would return A+2. We must seek to localCurrentMediaItemIndex instead.
            if (isCrossfading) {
                Logger.d(TAG, "seekToNext: Cancelling crossfade, seeking to track we're fading in (index $localCurrentMediaItemIndex)")
                coroutineScope.launch {
                    crossfadeJob?.cancel()
                    crossfadeJob = null
                    currentPlayerFilter?.enabled = false
                    secondaryPlayerFilter?.enabled = false
                    secondaryPlayer?.release()
                    secondaryPlayer = null
                    secondaryPlayerFilter = null
                    setCrossfading(false)
                }
                seekTo(localCurrentMediaItemIndex, 0)
                return
            }

            val nextIndex = getNextMediaItemIndex()
            seekTo(nextIndex, 0)
        }
    }

    override fun seekToPrevious() {
        // Cancel any ongoing crossfade first
        if (isCrossfading) {
            Logger.d(TAG, "seekToPrevious: Cancelling crossfade")
            coroutineScope.launch {
                crossfadeJob?.cancel()
                crossfadeJob = null
                currentPlayerFilter?.enabled = false
                secondaryPlayerFilter?.enabled = false
                secondaryPlayer?.release()
                secondaryPlayer = null
                secondaryPlayerFilter = null
                setCrossfading(false)
                if (crossfadeFromIndex >= 0) {
                    localCurrentMediaItemIndex = crossfadeFromIndex
                    playlist.getOrNull(crossfadeFromIndex)?.let { mediaItem ->
                        listeners.forEach {
                            it.onMediaItemTransition(
                                mediaItem,
                                PlayerConstants.MEDIA_ITEM_TRANSITION_REASON_SEEK,
                            )
                        }
                    }
                    forwardingPlayer.notifyMediaItemChanged()
                    crossfadeFromIndex = -1
                }
            }
        }

        // Standard music player behavior:
        // - Position > 3s  → seek to start of current track
        // - Position <= 3s → go to previous track
        val positionThresholdMs = 3000L
        if (cachedPosition > positionThresholdMs) {
            Logger.d(TAG, "seekToPrevious: pos=${cachedPosition}ms > ${positionThresholdMs}ms — seeking to start")
            currentPlayer?.seekTo(0)
            cachedPosition = 0
        } else if (hasPreviousMediaItem()) {
            Logger.d(TAG, "seekToPrevious: pos=${cachedPosition}ms <= ${positionThresholdMs}ms — going to previous track")
            val prevIndex = getPreviousMediaItemIndex()
            seekTo(prevIndex, 0)
        } else {
            Logger.d(TAG, "seekToPrevious: No previous item, seeking to start")
            currentPlayer?.seekTo(0)
            cachedPosition = 0
        }
    }

    override fun seekToPreviousMediaItem() {
        // Cancel any ongoing crossfade first (mirror seekToPrevious()).
        if (isCrossfading) {
            Logger.d(TAG, "seekToPreviousMediaItem: Cancelling crossfade")
            coroutineScope.launch {
                crossfadeJob?.cancel()
                crossfadeJob = null
                currentPlayerFilter?.enabled = false
                secondaryPlayerFilter?.enabled = false
                secondaryPlayer?.release()
                secondaryPlayer = null
                secondaryPlayerFilter = null
                setCrossfading(false)
                if (crossfadeFromIndex >= 0) {
                    localCurrentMediaItemIndex = crossfadeFromIndex
                    playlist.getOrNull(crossfadeFromIndex)?.let { mediaItem ->
                        listeners.forEach {
                            it.onMediaItemTransition(
                                mediaItem,
                                PlayerConstants.MEDIA_ITEM_TRANSITION_REASON_SEEK,
                            )
                        }
                    }
                    forwardingPlayer.notifyMediaItemChanged()
                    crossfadeFromIndex = -1
                }
            }
        }

        // Always advance to the previous track regardless of `cachedPosition` —
        // skips the 3-second "seek to start" rule used by seekToPrevious().
        if (hasPreviousMediaItem()) {
            val prevIndex = getPreviousMediaItemIndex()
            Logger.d(TAG, "seekToPreviousMediaItem: jumping to previous index=$prevIndex")
            seekTo(prevIndex, 0)
        } else {
            Logger.d(TAG, "seekToPreviousMediaItem: No previous item — no-op")
        }
    }

    override fun prepare() {
        if (playlist.isNotEmpty() && localCurrentMediaItemIndex >= 0) {
            coroutineScope.launch {
                loadAndPlayTrackInternal(localCurrentMediaItemIndex, 0, false)
            }
        }
    }

    // ========== Media Item Management ==========

    override fun setMediaItem(mediaItem: GenericMediaItem) {
        coroutineScope.launch {
            // Cancel ongoing operations
            currentLoadJob?.cancel()
            cancelPrecaching()

            playlist.clear()
            clearAllPrecacheInternal()
            playlist.add(mediaItem)
            localCurrentMediaItemIndex = 0

            if (internalShuffleModeEnabled) {
                createShuffleOrder()
            }

            notifyTimelineChanged("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED")
            loadAndPlayTrackInternal(0, 0, internalPlayWhenReady)
        }
    }

    override fun addMediaItem(mediaItem: GenericMediaItem) {
        playlist.add(mediaItem)

        if (internalShuffleModeEnabled) {
            createShuffleOrder()
        }

        notifyTimelineChanged("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED")

        if (playlist.size - 1 - currentMediaItemIndex <= maxPrecacheCount) {
            coroutineScope.launch {
                clearPrecacheExceptCurrentInternal()
                triggerPrecachingInternal()
            }
        }
    }

    override fun addMediaItem(
        index: Int,
        mediaItem: GenericMediaItem,
    ) {
        if (index in 0..playlist.size) {
            val currentIndexBeforeInsert = localCurrentMediaItemIndex

            playlist.add(index, mediaItem)

            // Adjust current index if needed
            if (index <= localCurrentMediaItemIndex) {
                localCurrentMediaItemIndex++
            }

            // Update shuffle order if enabled
            if (internalShuffleModeEnabled) {
                if (currentIndexBeforeInsert >= 0 && index == currentIndexBeforeInsert + 1) {
                    val currentShufflePos = shuffleIndices.getOrNull(currentIndexBeforeInsert) ?: 0
                    insertIntoShuffleOrder(index, currentShufflePos)
                } else {
                    createShuffleOrder()
                }
            }

            notifyTimelineChanged("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED")

            if (index - 1 - currentMediaItemIndex <= maxPrecacheCount) {
                coroutineScope.launch {
                    clearPrecacheExceptCurrentInternal()
                    triggerPrecachingInternal()
                }
            }
        }
    }

    override fun removeMediaItem(index: Int) {
        if (index !in playlist.indices) return

        coroutineScope.launch {
            val track = playlist.removeAt(index)

            // Remove from precache
            precachedPlayers.remove(track.mediaId)?.let { cached ->
                cleanupPlayerInternal(cached.player)
            }

            when {
                index < localCurrentMediaItemIndex -> {
                    localCurrentMediaItemIndex--
                    clearPrecacheExceptCurrentInternal()
                    triggerPrecachingInternal()
                }

                index == localCurrentMediaItemIndex -> {
                    if (localCurrentMediaItemIndex >= playlist.size) {
                        localCurrentMediaItemIndex = playlist.size - 1
                    }
                    if (localCurrentMediaItemIndex >= 0) {
                        loadAndPlayTrackInternal(localCurrentMediaItemIndex, 0, internalPlayWhenReady)
                    } else {
                        cleanupCurrentPlayerInternal()
                    }
                }

                else -> {
                    clearPrecacheExceptCurrentInternal()
                    triggerPrecachingInternal()
                }
            }

            if (internalShuffleModeEnabled) {
                createShuffleOrder()
            }

            notifyTimelineChanged("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED")
        }
    }

    override fun moveMediaItem(
        fromIndex: Int,
        toIndex: Int,
    ) {
        if (fromIndex !in playlist.indices || toIndex !in playlist.indices) return

        coroutineScope.launch {
            val item = playlist.removeAt(fromIndex)
            playlist.add(toIndex, item)

            // Update current index
            localCurrentMediaItemIndex =
                when {
                    localCurrentMediaItemIndex == fromIndex -> {
                        toIndex
                    }
                    fromIndex < localCurrentMediaItemIndex && toIndex >= localCurrentMediaItemIndex -> {
                        localCurrentMediaItemIndex - 1
                    }
                    fromIndex > localCurrentMediaItemIndex && toIndex <= localCurrentMediaItemIndex -> {
                        localCurrentMediaItemIndex + 1
                    }
                    else -> {
                        localCurrentMediaItemIndex
                    }
                }

            if (internalShuffleModeEnabled) {
                createShuffleOrder()
            }

            notifyTimelineChanged("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED")

            clearPrecacheExceptCurrentInternal()
            triggerPrecachingInternal()
        }
    }

    override fun clearMediaItems() {
        coroutineScope.launch {
            playlist.clear()
            localCurrentMediaItemIndex = -1
            clearShuffleOrder()
            notifyTimelineChanged("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED")
            cleanupCurrentPlayerInternal()
            clearAllPrecacheInternal()
        }
    }

    override fun replaceMediaItem(
        index: Int,
        mediaItem: GenericMediaItem,
    ) {
        if (index !in playlist.indices) return

        coroutineScope.launch {
            playlist[index] = mediaItem

            precachedPlayers.remove(mediaItem.mediaId)?.let { cached ->
                cleanupPlayerInternal(cached.player)
            }

            if (internalShuffleModeEnabled) {
                createShuffleOrder()
            }

            notifyTimelineChanged("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED")

            if (index == localCurrentMediaItemIndex) {
                loadAndPlayTrackInternal(index, 0, internalPlayWhenReady)
            } else {
                triggerPrecachingInternal()
            }
        }
    }

    override fun getMediaItemAt(index: Int): GenericMediaItem? = playlist.getOrNull(index)

    override fun getCurrentMediaTimeLine(): List<GenericMediaItem> =
        if (internalShuffleModeEnabled) {
            shuffleOrder.mapNotNull { shuffledIndex -> playlist.getOrNull(shuffledIndex) }
        } else {
            playlist.toList()
        }

    override fun getUnshuffledIndex(shuffledIndex: Int): Int =
        if (internalShuffleModeEnabled) {
            shuffleOrder.getOrNull(shuffledIndex) ?: -1
        } else {
            shuffledIndex
        }

    // ========== Playback State Properties ==========

    override val isPlaying: Boolean
        get() = internalState == InternalState.PLAYING

    override val currentPosition: Long
        get() = cachedPosition

    override val duration: Long
        get() = currentPlayer?.duration ?: cachedDuration

    override val bufferedPosition: Long
        get() = cachedBufferedPosition

    override val bufferedPercentage: Int
        get() {
            val dur = duration
            if (dur <= 0) return 0
            return ((bufferedPosition * 100) / dur).toInt().coerceIn(0, 100)
        }

    override val currentMediaItem: GenericMediaItem?
        get() = playlist.getOrNull(localCurrentMediaItemIndex)

    override val currentMediaItemIndex: Int
        get() = localCurrentMediaItemIndex

    override val mediaItemCount: Int
        get() = playlist.size

    override val contentPosition: Long
        get() = cachedPosition

    override val playbackState: Int
        get() =
            when (internalState) {
                InternalState.IDLE -> PlayerConstants.STATE_IDLE
                InternalState.PREPARING -> PlayerConstants.STATE_BUFFERING
                InternalState.READY -> PlayerConstants.STATE_READY
                InternalState.PLAYING -> PlayerConstants.STATE_READY
                InternalState.ENDED -> PlayerConstants.STATE_ENDED
                InternalState.ERROR -> PlayerConstants.STATE_IDLE
                InternalState.PAUSED -> PlayerConstants.STATE_READY
            }

    // ========== Navigation ==========

    override fun hasNextMediaItem(): Boolean =
        when (internalRepeatMode) {
            PlayerConstants.REPEAT_MODE_ONE -> true
            PlayerConstants.REPEAT_MODE_ALL -> true
            else -> localCurrentMediaItemIndex < playlist.size - 1
        }

    override fun hasPreviousMediaItem(): Boolean =
        when (internalRepeatMode) {
            PlayerConstants.REPEAT_MODE_ONE -> true
            PlayerConstants.REPEAT_MODE_ALL -> true
            else -> localCurrentMediaItemIndex > 0
        }

    private fun getNextMediaItemIndex(): Int =
        when (internalRepeatMode) {
            PlayerConstants.REPEAT_MODE_ONE -> {
                localCurrentMediaItemIndex
            }
            PlayerConstants.REPEAT_MODE_ALL -> {
                if (internalShuffleModeEnabled && shuffleOrder.isNotEmpty()) {
                    val currentShufflePos = shuffleIndices.getOrNull(localCurrentMediaItemIndex) ?: 0
                    val nextShufflePos = (currentShufflePos + 1) % shuffleOrder.size
                    shuffleOrder.getOrNull(nextShufflePos) ?: localCurrentMediaItemIndex
                } else {
                    if (localCurrentMediaItemIndex < playlist.size - 1) {
                        localCurrentMediaItemIndex + 1
                    } else {
                        0
                    }
                }
            }
            else -> {
                if (internalShuffleModeEnabled && shuffleOrder.isNotEmpty()) {
                    val currentShufflePos = shuffleIndices.getOrNull(localCurrentMediaItemIndex) ?: 0
                    val nextShufflePos = currentShufflePos + 1
                    if (nextShufflePos < shuffleOrder.size) {
                        shuffleOrder.getOrNull(nextShufflePos) ?: localCurrentMediaItemIndex
                    } else {
                        localCurrentMediaItemIndex
                    }
                } else {
                    (localCurrentMediaItemIndex + 1).coerceAtMost(playlist.size - 1)
                }
            }
        }

    private fun getPreviousMediaItemIndex(): Int =
        when (internalRepeatMode) {
            PlayerConstants.REPEAT_MODE_ONE -> {
                localCurrentMediaItemIndex
            }
            PlayerConstants.REPEAT_MODE_ALL -> {
                if (internalShuffleModeEnabled && shuffleOrder.isNotEmpty()) {
                    val currentShufflePos = shuffleIndices.getOrNull(localCurrentMediaItemIndex) ?: 0
                    val prevShufflePos =
                        if (currentShufflePos > 0) {
                            currentShufflePos - 1
                        } else {
                            shuffleOrder.size - 1
                        }
                    shuffleOrder.getOrNull(prevShufflePos) ?: localCurrentMediaItemIndex
                } else {
                    if (localCurrentMediaItemIndex > 0) {
                        localCurrentMediaItemIndex - 1
                    } else {
                        playlist.size - 1
                    }
                }
            }
            else -> {
                if (internalShuffleModeEnabled && shuffleOrder.isNotEmpty()) {
                    val currentShufflePos = shuffleIndices.getOrNull(localCurrentMediaItemIndex) ?: 0
                    val prevShufflePos = currentShufflePos - 1
                    if (prevShufflePos >= 0) {
                        shuffleOrder.getOrNull(prevShufflePos) ?: localCurrentMediaItemIndex
                    } else {
                        localCurrentMediaItemIndex
                    }
                } else {
                    (localCurrentMediaItemIndex - 1).coerceAtLeast(0)
                }
            }
        }

    // ========== Playback Modes ==========

    override var shuffleModeEnabled: Boolean
        get() = internalShuffleModeEnabled
        set(value) {
            if (internalShuffleModeEnabled == value) return

            internalShuffleModeEnabled = value

            if (value) {
                createShuffleOrder()
            } else {
                clearShuffleOrder()
            }

            val mediaItemList = getShuffledMediaItemList()
            listeners.forEach { it.onShuffleModeEnabledChanged(value, mediaItemList) }
            notifyTimelineChanged("TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED")

            Logger.d(TAG, "Shuffle mode ${if (value) "enabled" else "disabled"}")
        }

    override var repeatMode: Int
        get() = internalRepeatMode
        set(value) {
            if (internalRepeatMode == value) return
            internalRepeatMode = value
            listeners.forEach { it.onRepeatModeChanged(value) }
        }

    override var playWhenReady: Boolean
        get() = internalPlayWhenReady
        set(value) {
            internalPlayWhenReady = value
            if (value) play() else pause()
        }

    override var playbackParameters: GenericPlaybackParameters
        get() = GenericPlaybackParameters(internalPlaybackSpeed, internalPlaybackPitch)
        set(value) {
            internalPlaybackSpeed = value.speed
            internalPlaybackPitch = value.pitch
            val params = PlaybackParameters(value.speed, value.pitch)
            currentPlayer?.playbackParameters = params
            // Also apply to secondary player during crossfade
            secondaryPlayer?.playbackParameters = params
        }

    // ========== Audio Settings ==========

    override val audioSessionId: Int
        get() = currentPlayer?.audioSessionId ?: 0

    override var volume: Float
        get() = internalVolume
        set(value) {
            Logger.w(TAG, "Setting volume to $value")
            internalVolume = value.coerceIn(0f, 1f)
            currentPlayer?.volume = internalVolume
            listeners.forEach { it.onVolumeChanged(internalVolume) }
        }

    override var skipSilenceEnabled: Boolean
        get() = internalSkipSilence
        set(value) {
            internalSkipSilence = value
            currentPlayer?.skipSilenceEnabled = value
            // Also apply to secondary player during crossfade
            secondaryPlayer?.skipSilenceEnabled = value
        }

    // ========== Listener Management ==========

    override fun addListener(listener: MediaPlayerListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: MediaPlayerListener) {
        listeners.remove(listener)
    }

    // ========== Release Resources ==========

    override fun release() {
        // Cancel all ongoing jobs
        currentLoadJob?.cancel()
        precacheJob?.cancel()
        positionUpdateJob?.cancel()

        // Cancel crossfade
        crossfadeJob?.cancel()
        secondaryPlayer?.release()
        secondaryPlayer = null
        secondaryPlayerFilter = null
        currentPlayerFilter = null
        isCrossfading = false

        coroutineScope.cancel()
        cleanupCurrentPlayerInternal()
        clearAllPrecacheInternal()
        listeners.clear()
    }

    // ========== Internal: State Transition ==========

    /**
     * State transition helper - mirrors GstreamerPlayerAdapter.transitionToState()
     */
    private fun propagatePlayerError(error: PlaybackException) {
        val genericError =
            PlayerError(
                errorCode =
                    when (error.errorCode) {
                        PlaybackException.ERROR_CODE_TIMEOUT -> PlayerConstants.ERROR_CODE_TIMEOUT
                        else -> error.errorCode
                    },
                errorCodeName = error.errorCodeName,
                message = error.message,
            )
        Logger.e(TAG, "Playback error: ${error.message}")
        listeners.forEach { it.onPlayerError(genericError) }
        transitionToState(InternalState.ERROR)
    }

    private fun transitionToState(newState: InternalState) {
        if (internalState == newState) {
            Logger.d(TAG, "State transition ignored: already in $newState")
            return
        }

        val oldState = internalState
        internalState = newState

        Logger.d(TAG, "State: $oldState -> $newState (playWhenReady=$internalPlayWhenReady)")

        // Update cached duration from player
        currentPlayer?.let {
            val dur = it.duration
            if (dur > 0L) {
                cachedDuration = dur
            }
        }

        // Notify listeners
        when (newState) {
            InternalState.PAUSED -> {
                listeners.forEach { it.onPlaybackStateChanged(PlayerConstants.STATE_READY) }
                listeners.forEach { it.onIsPlayingChanged(false) }
            }

            InternalState.IDLE -> {
                listeners.forEach { it.onPlaybackStateChanged(PlayerConstants.STATE_IDLE) }
                listeners.forEach { it.onIsPlayingChanged(false) }
            }

            InternalState.PREPARING -> {
                listeners.forEach { it.onPlaybackStateChanged(PlayerConstants.STATE_BUFFERING) }
            }

            InternalState.READY -> {
                if (internalPlayWhenReady) {
                    play()
                } else {
                    listeners.forEach { it.onPlaybackStateChanged(PlayerConstants.STATE_READY) }
                    listeners.forEach { it.onIsPlayingChanged(false) }
                }
            }

            InternalState.PLAYING -> {
                listeners.forEach { it.onPlaybackStateChanged(PlayerConstants.STATE_READY) }
                listeners.forEach { it.onIsLoadingChanged(false) }
                listeners.forEach { it.onIsPlayingChanged(true) }
            }

            InternalState.ENDED -> {
                listeners.forEach { it.onPlaybackStateChanged(PlayerConstants.STATE_ENDED) }
                listeners.forEach { it.onIsPlayingChanged(false) }
            }

            InternalState.ERROR -> {
                listeners.forEach { it.onPlaybackStateChanged(PlayerConstants.STATE_IDLE) }
                listeners.forEach { it.onIsPlayingChanged(false) }
                listeners.forEach {
                    it.onPlayerError(
                        PlayerError(
                            errorCode = 403,
                            errorCodeName = "ERROR_UNKNOWN",
                            message = "Can not extract playable URL or playback error",
                        ),
                    )
                }
            }
        }
    }

    // ========== Internal: Load and Play Track ==========

    /**
     * Load and play track - mirrors GstreamerPlayerAdapter.loadAndPlayTrackInternal()
     *
     * Key difference: instead of extracting URL + creating PlayBin,
     * we create an ExoPlayer, set a MediaItem, and let MediaSourceFactory resolve the URL.
     */
    private fun loadAndPlayTrackInternal(
        index: Int,
        startPositionMs: Long,
        shouldPlay: Boolean,
    ) {
        if (index !in playlist.indices) return

        val mediaItem = playlist[index]
        val videoId = mediaItem.mediaId

        // Cancel previous load
        currentLoadJob?.cancel()

        currentLoadJob =
            coroutineScope.launch {
                try {
                    transitionToState(InternalState.PREPARING)

                    // Notify media item transition
                    listeners.forEach {
                        it.onMediaItemTransition(
                            mediaItem,
                            PlayerConstants.MEDIA_ITEM_TRANSITION_REASON_AUTO,
                        )
                    }

                    // Use precached player if available
                    val cachedPlayerEntry = precachedPlayers.remove(videoId)
                    val player: ExoPlayer
                    val playerFilter: CrossfadeFilterAudioProcessor?
                    if (cachedPlayerEntry?.player != null) {
                        Logger.d(TAG, "Using precached player for $videoId")
                        player = cachedPlayerEntry.player
                        playerFilter = cachedPlayerEntry.filter
                    } else {
                        Logger.d(TAG, "Creating new player for $videoId")
                        val pwf = createExoPlayerInstance(handleAudioFocus = false)
                        player = pwf.player
                        playerFilter = pwf.filter
                        player.setMediaItem(mediaItem.toMedia3MediaItem())
                        player.prepare()
                    }

                    // === CAREFUL ORDER for ForwardingPlayer integration ===

                    // 1. Remove our active listener from old player
                    cleanupPlayerListenerInternal()
                    stopPositionUpdates()
                    crossfadeJob?.cancel()
                    crossfadeJob = null
                    setCrossfading(false)

                    // 2. Save old player reference
                    val oldPlayer = currentPlayer

                    // 3. Set new player as current
                    currentPlayer = player
                    currentPlayerFilter = playerFilter

                    // 4. Setup our listener on new player
                    setupPlayerListenerInternal(player)

                    // 5. Swap ForwardingPlayer delegate (moves MediaSession's listeners from old to new)
                    forwardingPlayer.swapDelegate(player)

                    // 5b. Notify MediaSession about the new media item
                    // The MediaItem was set before the swap (either during precache or above),
                    // so MediaSession's listener missed the onMediaItemTransition event.
                    // play() below will trigger onIsPlayingChanged which causes MediaSession
                    // to re-query metadata, but this explicit notify is safer and ensures
                    // the notification updates immediately even if play() is delayed.
                    forwardingPlayer.notifyMediaItemChanged()

                    // 6. NOW release old player (it has no listeners anymore)
                    if (oldPlayer != null && oldPlayer !== player) {
                        try {
                            oldPlayer.stop()
                            oldPlayer.release()
                        } catch (e: Exception) {
                            Logger.w(TAG, "Error releasing old player: ${e.message}")
                        }
                    }

                    // Enable audio focus and headphone-disconnect handling on the current player
                    player.setAudioAttributes(audioAttributes, true)
                    player.setHandleAudioBecomingNoisy(true)

                    // Apply settings
                    player.volume = internalVolume
                    player.playbackParameters = PlaybackParameters(internalPlaybackSpeed, internalPlaybackPitch)
                    player.skipSilenceEnabled = internalSkipSilence

                    // Seek if needed
                    if (startPositionMs > 0) {
                        player.seekTo(startPositionMs)
                        cachedPosition = startPositionMs
                    }

                    // Auto-play if requested
                    if (shouldPlay) {
                        player.play()
                        transitionToState(InternalState.PLAYING)
                    } else {
                        player.pause()
                        transitionToState(InternalState.READY)
                    }

                    // Start position updates
                    startPositionUpdates()

                    // Eagerly load audio metadata for auto crossfade calculations
                    // so it's available when position updates check the trigger threshold
                    if (crossfadeEnabled && crossfadeDurationMs == DataStoreManager.CROSSFADE_DURATION_AUTO) {
                        loadAudioMetaIfNeeded(videoId)
                    }

                    // Trigger precaching
                    triggerPrecachingInternal()
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Logger.e(TAG, "Load track error: ${e.message}", e)
                    transitionToState(InternalState.ERROR)
                }
            }
    }

    // ========== Internal: Player Listener Management ==========
    // Equivalent to setupPlayerListenersInternal / cleanupBusListenersInternal

    /**
     * Setup Player.Listener on the given player.
     * First removes any existing listener from the old player (like cleanupBusListenersInternal),
     * then creates and attaches a new listener to the given player.
     *
     * This is the KEY crossfade mechanism:
     * When crossfade starts, the listener is moved from old player to new player.
     * The old player's STATE_ENDED event is then ignored (no listener to handle it).
     */
    private fun setupPlayerListenerInternal(player: ExoPlayer) {
        // Clean up old listener first
        cleanupPlayerListenerInternal()

        val listener =
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            Logger.d(TAG, "End of stream reached")
                            transitionToState(InternalState.ENDED)
                            handleTrackEndInternal()
                        }

                        Player.STATE_READY -> {
                            // Always clear loading state when ExoPlayer is ready
                            // (handles both initial load AND mid-playback rebuffer)
                            if (cachedIsLoading && player == currentPlayer) {
                                cachedIsLoading = false
                                listeners.forEach { it.onIsLoadingChanged(false) }
                            }
                            // Duration should be available now
                            val dur = player.duration
                            if (dur > 0) cachedDuration = dur
                            // Reset retry counter on successful playback
                            retryCount = 0
                            retryVideoId = null
                        }

                        Player.STATE_BUFFERING -> {
                            // Playback is stalled waiting for data — report buffering
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (player != currentPlayer) {
                        Logger.d(TAG, "Ignoring onPlaybackStateChanged from non-current player")
                        return
                    }
                    if (isPlaying) {
                        if (internalState != InternalState.PLAYING) {
                            transitionToState(InternalState.PLAYING)
                            notifyEqualizerIntent(true)
                        }
                    } else {
                        if (internalState == InternalState.PLAYING) {
                            if (!player.playWhenReady) {
                                transitionToState(InternalState.PAUSED)
                                notifyEqualizerIntent(false)
                            }
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    if (player != currentPlayer) {
                        Logger.d(TAG, "Ignoring onPlayerError from non-current player")
                        return
                    }

                    // Retry for source errors (expired/invalid stream URL)
                    // ERROR_CODE_PARSING_CONTAINER_MALFORMED (3001) = server returned non-media response (e.g. HTML error page)
                    // ERROR_CODE_IO_BAD_HTTP_STATUS (2004) = HTTP 403/410 from expired URL
                    // ERROR_CODE_IO_NETWORK_CONNECTION_FAILED (2001) = connection refused
                    val isRetryableSourceError =
                        error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ||
                            error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS

                    val currentVideoId = playlist.getOrNull(localCurrentMediaItemIndex)?.mediaId
                    if (isRetryableSourceError && currentVideoId != null) {
                        // Reset retry count if this is a different track
                        if (retryVideoId != currentVideoId) {
                            retryVideoId = currentVideoId
                            retryCount = 0
                        }
                        if (retryCount < maxRetryCount) {
                            retryCount++
                            Logger.w(TAG, "Retryable source error (attempt $retryCount/$maxRetryCount) for $currentVideoId: ${error.errorCodeName}")
                            coroutineScope.launch {
                                try {
                                    // Invalidate cached format so ResolvingDataSource fetches a fresh URL
                                    streamRepository.invalidateFormat(currentVideoId)
                                    streamRepository.invalidateFormat("${com.maxrave.common.MERGING_DATA_TYPE.VIDEO}$currentVideoId")
                                    // Evict from precache (it may hold a stale player)
                                    precachedPlayers.remove(currentVideoId)?.player?.release()
                                    // Reload the track
                                    loadAndPlayTrackInternal(localCurrentMediaItemIndex, 0L, shouldPlay = true)
                                } catch (e: Exception) {
                                    if (e is CancellationException) throw e
                                    Logger.e(TAG, "Retry failed: ${e.message}", e)
                                    propagatePlayerError(error)
                                }
                            }
                            return
                        }
                        Logger.e(TAG, "Max retries ($maxRetryCount) exhausted for $currentVideoId")
                    }

                    propagatePlayerError(error)
                }

                override fun onIsLoadingChanged(isLoading: Boolean) {
                    // ExoPlayer reports isLoading=true during normal background buffer refill,
                    // not just when playback is stalled. Only propagate loading=true when
                    // playback is actually stalled (STATE_BUFFERING), otherwise the UI
                    // would show a buffering indicator continuously during normal playback.
                    val isPlaybackStalled = isLoading && player.playbackState == Player.STATE_BUFFERING
                    val isCurrentPlayer = player == currentPlayer
                    Logger.d(TAG, "onIsLoadingChanged: isLoading=$isLoading, isPlaybackStalled=$isPlaybackStalled, isCurrentPlayer=$isCurrentPlayer")
                    if (cachedIsLoading != isPlaybackStalled && isCurrentPlayer) {
                        cachedIsLoading = isPlaybackStalled
                        listeners.forEach { it.onIsLoadingChanged(isPlaybackStalled) }
                    }
                }

                override fun onTracksChanged(tracks: Tracks) {
                    if (player != currentPlayer) {
                        Logger.d(TAG, "Ignoring onPlaybackStateChanged from non-current player")
                        return
                    }
                    val genericTracks = tracks.toGenericTracks()
                    listeners.forEach { it.onTracksChanged(genericTracks) }
                }

                override fun onEvents(
                    player: Player,
                    events: Player.Events,
                ) {
                    if (player != currentPlayer) {
                        Logger.d(TAG, "Ignoring onPlaybackStateChanged from non-current player")
                        return
                    }
                    val shouldBePlaying =
                        !(player.playbackState == Player.STATE_ENDED || !player.playWhenReady)
                    if (events.containsAny(
                            Player.EVENT_PLAYBACK_STATE_CHANGED,
                            Player.EVENT_PLAY_WHEN_READY_CHANGED,
                            Player.EVENT_IS_PLAYING_CHANGED,
                            Player.EVENT_POSITION_DISCONTINUITY,
                        )
                    ) {
                        if (shouldBePlaying) {
                            listeners.forEach { it.shouldOpenOrCloseEqualizerIntent(true) }
                        } else {
                            listeners.forEach { it.shouldOpenOrCloseEqualizerIntent(false) }
                        }
                    }
                }
            }

        player.addListener(listener)
        activePlayerListener = listener
    }

    /**
     * Clean up active player listener from whichever player has it.
     * During crossfade the listener is on secondaryPlayer, not currentPlayer.
     */
    private fun cleanupPlayerListenerInternal() {
        activePlayerListener?.let { listener ->
            currentPlayer?.removeListener(listener)
            secondaryPlayer?.removeListener(listener)
        }
        activePlayerListener = null
    }

    // ========== Internal: Player Cleanup ==========

    private fun cleanupPlayerInternal(player: ExoPlayer) {
        try {
            player.stop()
            player.release()
        } catch (e: Exception) {
            Logger.w(TAG, "Error cleaning up player: ${e.message}")
        }
    }

    private fun cleanupCurrentPlayerInternal() {
        stopPositionUpdates()
        cleanupPlayerListenerInternal()

        // Cancel any ongoing crossfade
        crossfadeJob?.cancel()
        crossfadeJob = null
        setCrossfading(false)

        currentPlayer?.let { cleanupPlayerInternal(it) }
        currentPlayer = null
    }

    // ========== Internal: Track End ==========

    /**
     * Handle track end - mirrors GstreamerPlayerAdapter.handleTrackEndInternal()
     */
    private fun handleTrackEndInternal() {
        // Check if crossfade should be used
        val shouldCrossfade =
            crossfadeEnabled &&
                hasNextMediaItem() &&
                !isCrossfading

        if (shouldCrossfade) {
            val nextIndex = getNextMediaItemIndex()
            triggerCrossfadeTransition(nextIndex)
        } else {
            // Original behavior
            when (internalRepeatMode) {
                PlayerConstants.REPEAT_MODE_ONE -> {
                    seekTo(localCurrentMediaItemIndex, 0)
                }

                PlayerConstants.REPEAT_MODE_ALL -> {
                    if (hasNextMediaItem()) {
                        seekToNext()
                    }
                }

                else -> {
                    if (localCurrentMediaItemIndex < playlist.size - 1) {
                        seekToNext()
                    } else {
                        notifyEqualizerIntent(false)
                    }
                }
            }
        }
    }

    // ========== Internal: Crossfade ==========

    /**
     * Trigger crossfade to next track.
     * Mirrors GstreamerPlayerAdapter.triggerCrossfadeTransition()
     *
     * Key mechanism: [setupPlayerListenerInternal] moves the active listener
     * from the current player to the next player. The old player's STATE_ENDED
     * event is then ignored (no listener to fire).
     */
    private fun triggerCrossfadeTransition(nextIndex: Int) {
        if (nextIndex !in playlist.indices || isCrossfading) return

        coroutineScope.launch {
            try {
                setCrossfading(true)
                val nextMediaItem = playlist[nextIndex]
                val nextVideoId = nextMediaItem.mediaId

                Logger.d(TAG, "Starting crossfade to track $nextIndex")

                // Get or create secondary player
                val cachedPlayerEntry = precachedPlayers.remove(nextVideoId)
                val nextPlayer: ExoPlayer
                val nextFilter: CrossfadeFilterAudioProcessor?
                if (cachedPlayerEntry?.player != null) {
                    nextPlayer = cachedPlayerEntry.player
                    nextFilter = cachedPlayerEntry.filter
                } else {
                    val pwf = createExoPlayerInstance(handleAudioFocus = false)
                    nextPlayer = pwf.player
                    nextFilter = pwf.filter
                    nextPlayer.setMediaItem(nextMediaItem.toMedia3MediaItem())
                    nextPlayer.prepare()
                }

                // Setup secondary player
                secondaryPlayer = nextPlayer
                secondaryPlayerFilter = nextFilter
                // *** KEY: Move our custom listener from current to next player ***
                setupPlayerListenerInternal(nextPlayer)
                // Playback parameters applied below after AutoMix ratios are calculated
                nextPlayer.skipSilenceEnabled = internalSkipSilence
                nextPlayer.volume = 0f

                // === CRITICAL ORDER for MediaSession notification ===
                // 1. Swap ForwardingPlayer BEFORE play()
                //    This moves MediaSession's Player.Listener to nextPlayer.
                //    If we play() first, MediaSession misses onIsPlayingChanged and
                //    onMediaItemTransition events (they fire before its listener is attached).
                forwardingPlayer.swapDelegate(nextPlayer)

                // 2. Now play - MediaSession's listener is attached and receives state change events
                nextPlayer.play()

                // 3. Force MediaSession to update notification metadata
                //    Even though play() triggers onIsPlayingChanged (which causes MediaSession
                //    to re-query player state), the onMediaItemTransition event was missed
                //    (MediaItem was set during precache, before the swap).
                //    This explicitly notifies MediaSession about the new track metadata.
                forwardingPlayer.notifyMediaItemChanged()

                // Capture current video ID BEFORE advancing localCurrentMediaItemIndex
                val currentVideoId = playlist.getOrNull(localCurrentMediaItemIndex)?.mediaId ?: ""

                // Lazily load AutoMix metadata from NewFormatEntity if not in cache
                val isAutoMode = crossfadeDurationMs == DataStoreManager.CROSSFADE_DURATION_AUTO
                if (isAutoMode) {
                    loadAudioMetaIfNeeded(currentVideoId)
                    loadAudioMetaIfNeeded(nextVideoId)
                }
                val resolvedConfigDurationMs =
                    if (isAutoMode) {
                        resolveAutoCrossfadeDurationMs(currentVideoId, nextVideoId)
                    } else {
                        crossfadeDurationMs
                    }
                val bpmSpeedRatio = if (isAutoMode) calculateBpmSpeedRatio(currentVideoId, nextVideoId) else 1.0f
                val keyPitchRatio = if (isAutoMode) calculateKeyPitchRatio(currentVideoId, nextVideoId) else 1.0f

                // Incoming player plays at natural speed/pitch — only the outgoing
                // player is adjusted to match the incoming track during crossfade.
                nextPlayer.playbackParameters =
                    PlaybackParameters(internalPlaybackSpeed, internalPlaybackPitch)

                // Update now playing IMMEDIATELY (store from-index for cancel scenarios)
                crossfadeFromIndex = localCurrentMediaItemIndex
                localCurrentMediaItemIndex = nextIndex

                // Notify our custom listeners IMMEDIATELY (UI updates to new track)
                listeners.forEach {
                    it.onMediaItemTransition(
                        nextMediaItem,
                        PlayerConstants.MEDIA_ITEM_TRANSITION_REASON_AUTO,
                    )
                }

                Logger.d(TAG, "Now playing updated to track $nextIndex during crossfade")

                // Calculate effective crossfade duration based on ACTUAL remaining time.
                // If the secondary player wasn't precached, URL resolution + buffering may
                // have consumed part of the crossfade window. Use the lesser of configured
                // duration and actual remaining time so the animation ends when the old track does.
                val actualTimeRemaining =
                    currentPlayer?.let { player ->
                        val dur = player.duration
                        val pos = player.currentPosition
                        // Divide by playback speed: at 2x speed, remaining wall-clock time is halved
                        val speed = internalPlaybackSpeed.coerceAtLeast(0.1f)
                        if (dur > 0 && pos >= 0) ((dur - pos) / speed).toLong() else resolvedConfigDurationMs.toLong()
                    } ?: resolvedConfigDurationMs.toLong()

                val effectiveCrossfadeDurationMs =
                    minOf(resolvedConfigDurationMs.toLong(), actualTimeRemaining)
                        .coerceAtLeast(1000L)
                        .toInt()

                Logger.d(
                    TAG,
                    "Crossfade duration: configured=${resolvedConfigDurationMs}ms (auto=$isAutoMode), " +
                        "bpmRatio=$bpmSpeedRatio, pitchRatio=$keyPitchRatio, " +
                        "actualRemaining=${actualTimeRemaining}ms, effective=${effectiveCrossfadeDurationMs}ms",
                )

                // Perform crossfade animation with effective duration and AutoMix parameters
                performCrossfade(nextIndex, nextPlayer, effectiveCrossfadeDurationMs, bpmSpeedRatio, keyPitchRatio)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.e(TAG, "Crossfade error: ${e.message}", e)
                setCrossfading(false)
                // Fallback to normal transition
                seekTo(nextIndex, 0)
            }
        }
    }

    /**
     * S-curve (sigmoid) function for DJ filter crossfade timing.
     * Keeps both tracks near full spectrum at the start and end,
     * with a steep transition in the middle — like a real DJ mixer crossfader.
     *
     * k controls steepness: higher = sharper transition.
     * k=6 gives a gentle S-curve: ~8-92% of duration covers the sweep,
     * keeping a subtle onset/ending without harsh "slam" in the middle.
     */
    private fun sigmoid(
        t: Float,
        k: Float = DJ_FILTER_SIGMOID_K,
    ): Float = 1.0f / (1.0f + exp(-k * (t - 0.5f)))

    /**
     * Exponential interpolation between two values.
     * Frequency perception is logarithmic, so this produces a natural-sounding sweep.
     */
    private fun exponentialInterpolate(
        start: Float,
        end: Float,
        t: Float,
    ): Float {
        if (start <= 0f || end <= 0f) return end
        return exp(ln(start) + (ln(end) - ln(start)) * t).toFloat()
    }

    /**
     * Perform the actual crossfade animation.
     * Mirrors GstreamerPlayerAdapter.performCrossfade()
     *
     * @param effectiveDurationMs The actual crossfade duration to use. May be shorter than
     *   the configured [crossfadeDurationMs] if URL resolution / buffering consumed
     *   part of the crossfade window.
     * @param targetSpeedRatio BPM-based speed ratio for incoming track (1.0 = no adjustment).
     *   Ramps from this value to 1.0 during crossfade so the track plays at natural speed after.
     * @param targetPitchRatio Key-based pitch ratio for incoming track (1.0 = no adjustment).
     *   Ramps from this value to 1.0 during crossfade.
     */
    private suspend fun performCrossfade(
        nextIndex: Int,
        nextPlayer: ExoPlayer,
        effectiveDurationMs: Int,
        targetSpeedRatio: Float = 1.0f,
        targetPitchRatio: Float = 1.0f,
    ) {
        val steps = 50 // 50 steps for smooth transition
        val delayPerStep = (effectiveDurationMs / steps).coerceAtLeast(20) // min 20ms per step
        val targetVolume = internalVolume
        val useDjFilter = djCrossfadeEnabled
        val useAutoMixRamp = targetSpeedRatio != 1.0f || targetPitchRatio != 1.0f
        Logger.d(
            TAG,
            "Crossfade animation: ${effectiveDurationMs}ms, $steps steps, ${delayPerStep}ms/step, " +
                "dj=$useDjFilter, autoMix=$useAutoMixRamp (speed=$targetSpeedRatio, pitch=$targetPitchRatio)",
        )

        // Setup DJ filters before starting animation
        if (useDjFilter) {
            currentPlayerFilter?.let { filter ->
                filter.filterType = BiquadFilter.FilterType.LOW_PASS
                filter.cutoffFrequencyHz = LPF_START_HZ
                filter.enabled = true
            }
            secondaryPlayerFilter?.let { filter ->
                filter.filterType = BiquadFilter.FilterType.HIGH_PASS
                filter.cutoffFrequencyHz = HPF_START_HZ
                filter.enabled = true
            }
        }

        // Track last quantized speed/pitch to avoid redundant PlaybackParameters updates
        var lastOutgoingSpeed = -1f
        var lastOutgoingPitch = -1f

        crossfadeJob?.cancel()
        crossfadeJob =
            coroutineScope.launch {
                try {
                    for (step in 0..steps) {
                        if (!isActive) break

                        val progress = step.toFloat() / steps

                        // Fade out current player (old track)
                        val fadeOutVolume = targetVolume * (1f - progress)
                        currentPlayer?.volume = fadeOutVolume

                        // Fade in next player (new track)
                        val fadeInVolume = targetVolume * progress
                        nextPlayer.volume = fadeInVolume

                        // DJ-style filter sweep (alongside volume)
                        // S-curve (sigmoid) on time axis: holds flat at start/end,
                        // transitions steeply in the middle — mimics a real DJ mixer
                        // crossfader where both tracks briefly overlap at full spectrum.
                        // Exponential interpolation on frequency axis preserves
                        // logarithmic hearing perception for a natural-sounding sweep.
                        if (useDjFilter) {
                            val filterProgress = sigmoid(progress)

                            // Outgoing: LPF sweeps 20kHz → 200Hz
                            currentPlayerFilter?.cutoffFrequencyHz =
                                exponentialInterpolate(LPF_START_HZ, LPF_END_HZ, filterProgress)

                            // Incoming: HPF sweeps 8kHz → 20Hz
                            secondaryPlayerFilter?.cutoffFrequencyHz =
                                exponentialInterpolate(HPF_START_HZ, HPF_END_HZ, filterProgress)
                        }

                        // AutoMix: only adjust the OUTGOING (previous) player to match
                        // the incoming track. The incoming player stays at natural speed/pitch.
                        // Outgoing: ramp from natural (1.0) → targetRatio
                        // Quantize to SPEED_PITCH_STEP to avoid SonicAudioProcessor
                        // popping from too-frequent micro-adjustments
                        if (useAutoMixRamp) {
                            // Outgoing player: natural → target ratio (match incoming track)
                            val rawOutSpeed = lerp(1.0f, targetSpeedRatio, progress)
                            val rawOutPitch = lerp(1.0f, targetPitchRatio, progress)
                            val qOutSpeed = quantize(rawOutSpeed * internalPlaybackSpeed)
                            val qOutPitch = quantize(rawOutPitch * internalPlaybackPitch)

                            if (qOutSpeed != lastOutgoingSpeed || qOutPitch != lastOutgoingPitch) {
                                currentPlayer?.playbackParameters = PlaybackParameters(qOutSpeed, qOutPitch)
                                lastOutgoingSpeed = qOutSpeed
                                lastOutgoingPitch = qOutPitch
                            }
                        }

                        delay(delayPerStep.toLong())
                    }

                    // Transition complete
                    finalizeCrossfade(nextIndex, nextPlayer)
                } catch (e: CancellationException) {
                    Logger.d(TAG, "Crossfade cancelled")
                    // Cleanup DJ filters
                    currentPlayerFilter?.enabled = false
                    secondaryPlayerFilter?.enabled = false
                    // Restore outgoing player's speed/pitch to natural
                    currentPlayer?.playbackParameters =
                        PlaybackParameters(internalPlaybackSpeed, internalPlaybackPitch)
                    // Cleanup player
                    nextPlayer.release()
                    secondaryPlayer = null
                    secondaryPlayerFilter = null
                    setCrossfading(false)
                }
            }
    }

    // ========== AutoMix Public API ==========

    /**
     * Audio analysis metadata for a song, populated from Tidal search response.
     */
    data class SongAudioMeta(
        val bpm: Int?,
        val key: String?,
        val keyScale: String?, // "MAJOR" or "MINOR"
    )

    /**
     * Update the audio analysis metadata cache for a song.
     * Called externally when Tidal 320kbps stream is fetched and BPM/key data is available.
     * Data is used by Auto crossfade mode for beat-quantized duration, BPM matching, and key matching.
     */
    fun updateSongAudioMeta(
        videoId: String,
        bpm: Int?,
        key: String?,
        keyScale: String?,
    ) {
        if (bpm != null || key != null) {
            audioMetaCache[videoId] = SongAudioMeta(bpm, key, keyScale)
            Logger.d(TAG, "AutoMix meta updated: videoId=$videoId, bpm=$bpm, key=$key $keyScale")
        }
    }

    // ========== AutoMix Internal Logic ==========

    /**
     * Lazily load audio analysis metadata from NewFormatEntity if not already in cache.
     * Called before AutoMix calculations to ensure metadata is available.
     */
    private suspend fun loadAudioMetaIfNeeded(videoId: String) {
        if (videoId.isBlank() || audioMetaCache.containsKey(videoId)) return
        try {
            val format = streamRepository.getNewFormat(videoId).firstOrNull()
            if (format == null) {
                Logger.d(TAG, "AutoMix meta: no NewFormatEntity found for videoId=$videoId")
                return
            }
            if (format.bpm != null || format.musicKey != null) {
                audioMetaCache[videoId] = SongAudioMeta(format.bpm, format.musicKey, format.keyScale)
                Logger.d(TAG, "AutoMix meta loaded: videoId=$videoId, bpm=${format.bpm}, key=${format.musicKey} ${format.keyScale}")
            } else {
                Logger.d(TAG, "AutoMix meta: format exists but no bpm/key data for videoId=$videoId")
            }
        } catch (e: Exception) {
            Logger.w(TAG, "Failed to load AutoMix meta for $videoId: ${e.message}")
        }
    }

    /**
     * Linear interpolation between two values.
     */
    private fun lerp(
        start: Float,
        end: Float,
        t: Float,
    ): Float = start + (end - start) * t

    /**
     * Quantize a speed/pitch value to the nearest [SPEED_PITCH_STEP] (2%).
     * Prevents SonicAudioProcessor from resetting on micro-adjustments that
     * cause audible popping/clicking artifacts.
     */
    private fun quantize(value: Float): Float = (Math.round(value / SPEED_PITCH_STEP) * SPEED_PITCH_STEP)

    /**
     * Get BPM-adaptive target duration for auto crossfade.
     * Linear interpolation: BPM 70 → 35s, BPM 170 → 12s.
     * Slower songs get longer crossfade (like Apple Music AutoMix ~30s average).
     */
    private fun getAutoTargetDurationMs(bpm: Int): Double {
        val clampedBpm = bpm.coerceIn(70, 170)
        // Linear interpolation: BPM 70 → 30s, BPM 170 → 7s
        return 30000.0 - (clampedBpm - 70) * 230.0
    }

    /**
     * Resolve the crossfade duration for Auto mode based on BPM data.
     * Chooses the beat count (4, 8, or 16) that produces a duration closest to ~8 seconds.
     * Falls back to [AUTO_FALLBACK_DURATION_MS] when no BPM data is available.
     */
    private fun resolveAutoCrossfadeDurationMs(
        currentVideoId: String,
        nextVideoId: String,
    ): Int {
        val currentBpm = audioMetaCache[currentVideoId]?.bpm
        val nextBpm = audioMetaCache[nextVideoId]?.bpm
        // Use current song's BPM (or next song's as fallback) for beat-quantized duration
        val bpm = currentBpm ?: nextBpm ?: return AUTO_FALLBACK_DURATION_MS

        if (bpm <= 0) return AUTO_FALLBACK_DURATION_MS

        val beatMs = 60_000.0 / bpm
        // BPM-adaptive target: slow songs get longer crossfade, fast songs shorter
        // Linear interpolation: BPM 70 → 35s, BPM 170 → 12s
        val targetMs = getAutoTargetDurationMs(bpm)
        val bestBeatCount =
            BEAT_COUNT_OPTIONS.minByOrNull { abs(it * beatMs - targetMs) }
                ?: DEFAULT_BEAT_COUNT
        val duration = (bestBeatCount * beatMs).toInt()

        return duration.coerceIn(AUTO_MIN_DURATION_MS, AUTO_MAX_DURATION_MS)
    }

    /**
     * Calculate the playback speed ratio to match the incoming track's BPM to the current track's BPM.
     * Returns a ratio to apply to the incoming track's speed (e.g., 1.05 = speed up 5%).
     * Handles halftime/doubletime harmonic BPM relationships (e.g., 70 BPM ≈ 140 BPM / 2).
     * Returns 1.0 if no BPM data or the ratio exceeds the safe adjustment range.
     */
    private fun calculateBpmSpeedRatio(
        currentVideoId: String,
        nextVideoId: String,
    ): Float {
        val currentMeta = audioMetaCache[currentVideoId]
        val nextMeta = audioMetaCache[nextVideoId]
        val currentBpm = currentMeta?.bpm
        val nextBpm = nextMeta?.bpm

        if (currentBpm == null || nextBpm == null) {
            Logger.d(
                TAG,
                "AutoMix BPM: missing data - current=$currentBpm (cached=${currentMeta != null}), " +
                    "next=$nextBpm (cached=${nextMeta != null})",
            )
            return 1.0f
        }
        if (currentBpm <= 0 || nextBpm <= 0) return 1.0f

        var ratio = currentBpm.toFloat() / nextBpm.toFloat()

        // Normalize halftime/doubletime relationships (e.g., 140/70 → 1.0, 70/140 → 1.0)
        while (ratio > 1.5f) ratio /= 2f
        while (ratio < 0.67f) ratio *= 2f

        Logger.d(TAG, "AutoMix BPM: current=$currentBpm, next=$nextBpm, ratio=${"%.4f".format(ratio)}")

        // Only apply if adjustment is within safe range (avoids unnatural artifacts)
        // Quantize to SPEED_PITCH_STEP to avoid SonicAudioProcessor artifacts
        return if (ratio in BPM_RATIO_MIN..BPM_RATIO_MAX) {
            quantize(ratio)
        } else {
            Logger.d(TAG, "AutoMix BPM: ratio ${"%.4f".format(ratio)} outside safe range [$BPM_RATIO_MIN..$BPM_RATIO_MAX], skipping")
            1.0f
        }
    }

    // ========== Camelot Wheel Key Matching ==========

    /**
     * Camelot Wheel position: number (1-12) + type (minor=A, major=B).
     * Standard DJ key compatibility system — adjacent codes are harmonically compatible.
     */
    private data class CamelotCode(
        val number: Int,
        val isMinor: Boolean,
    ) {
        override fun toString(): String = "$number${if (isMinor) "A" else "B"}"
    }

    /**
     * Map a musical key + scale to its Camelot Wheel code.
     *
     * Camelot Wheel (minor = A, major = B):
     *  1A=Abm  1B=B     |  7A=Dm   7B=F
     *  2A=Ebm  2B=F#    |  8A=Am   8B=C
     *  3A=Bbm  3B=Db    |  9A=Em   9B=G
     *  4A=Fm   4B=Ab    | 10A=Bm  10B=D
     *  5A=Cm   5B=Eb    | 11A=F#m 11B=A
     *  6A=Gm   6B=Bb    | 12A=C#m 12B=E
     */
    private fun keyToCamelot(
        key: String,
        keyScale: String?,
    ): CamelotCode? {
        val semitone = keyToSemitone(key)
        if (semitone < 0) return null

        val isMinor = keyScale?.uppercase()?.contains("MIN") == true

        // Camelot number lookup by chromatic semitone (C=0, C#=1, D=2, ... B=11)
        //                                   C  C# D  D# E  F  F# G  G# A  A# B
        val minorCamelotByPitch = intArrayOf(5, 12, 7, 2, 9, 4, 11, 6, 1, 8, 3, 10)
        val majorCamelotByPitch = intArrayOf(8, 3, 10, 5, 12, 7, 2, 9, 4, 11, 6, 1)

        val number = if (isMinor) minorCamelotByPitch[semitone] else majorCamelotByPitch[semitone]
        return CamelotCode(number, isMinor)
    }

    /**
     * Calculate the Camelot distance between two keys.
     * Considers both the circular number distance (0-6) and type difference (A/B).
     *
     * Compatible transitions (distance <= 1):
     * - Same code (8A->8A): distance 0
     * - Adjacent number, same type (8A->7A, 8A->9A): distance 1
     * - Same number, different type / relative key (8A->8B): distance 1
     *
     * Near-compatible (distance 2, same type):
     * - +-2 same type (8A->10A): 2 semitones apart (whole tone) — safe to shift
     */
    private fun camelotDistance(
        a: CamelotCode,
        b: CamelotCode,
    ): Int {
        val numberDiff = abs(a.number - b.number)
        val circularDist = minOf(numberDiff, 12 - numberDiff)
        val typeDiff = if (a.isMinor != b.isMinor) 1 else 0
        return circularDist + typeDiff
    }

    /**
     * Calculate pitch ratio using Camelot Wheel for musically correct key matching.
     *
     * Rules based on music theory:
     * 1. Camelot distance <= 1: already harmonically compatible -> no shift
     * 2. Camelot distance = 2, same type: whole tone (2 semitones) -> safe to shift
     * 3. Otherwise: too far or cross-type -> don't shift (would cause artifacts)
     *
     * Only shifts by whole tone (2 semitones) — the smallest interval that
     * stays within both major and minor scales and avoids dissonance.
     */
    private fun calculateKeyPitchRatio(
        currentVideoId: String,
        nextVideoId: String,
    ): Float {
        val currentMeta = audioMetaCache[currentVideoId]
        val nextMeta = audioMetaCache[nextVideoId]
        val currentKey = currentMeta?.key
        val nextKey = nextMeta?.key

        if (currentKey == null || nextKey == null) {
            Logger.d(
                TAG,
                "AutoMix Key: missing data - currentKey=$currentKey (cached=${currentMeta != null}), " +
                    "nextKey=$nextKey (cached=${nextMeta != null})",
            )
            return 1.0f
        }

        val currentCamelot = keyToCamelot(currentKey, currentMeta.keyScale)
        val nextCamelot = keyToCamelot(nextKey, nextMeta.keyScale)

        if (currentCamelot == null || nextCamelot == null) {
            Logger.d(
                TAG,
                "AutoMix Key: unknown key format - currentKey='$currentKey' ${currentMeta.keyScale}, " +
                    "nextKey='$nextKey' ${nextMeta.keyScale}",
            )
            return 1.0f
        }

        val dist = camelotDistance(currentCamelot, nextCamelot)

        Logger.d(
            TAG,
            "AutoMix Key: current=$currentKey ${currentMeta.keyScale} ($currentCamelot), " +
                "next=$nextKey ${nextMeta.keyScale} ($nextCamelot), camelotDist=$dist",
        )

        // Already harmonically compatible — no shift needed
        if (dist <= 1) {
            Logger.d(TAG, "AutoMix Key: compatible (dist=$dist), no shift")
            return 1.0f
        }

        // Distance 2, same type: exactly a whole tone (2 semitones) apart
        // This is the only safe shift — stays within scale degrees
        if (dist == 2 && currentCamelot.isMinor == nextCamelot.isMinor) {
            val currentSemitone = keyToSemitone(currentKey)
            val nextSemitone = keyToSemitone(nextKey)

            // Chromatic direction (shortest path around circle)
            var chromDiff = (nextSemitone - currentSemitone + 12) % 12
            if (chromDiff > 6) chromDiff -= 12

            // Sanity check: Camelot distance 2 same type = +-2 semitones (whole tone)
            if (abs(chromDiff) != 2) {
                Logger.d(TAG, "AutoMix Key: unexpected chromDiff=$chromDiff for Camelot dist=2, skipping")
                return 1.0f
            }

            // Shift incoming track by whole tone to match current key
            val pitchRatio = exp(ln(2.0) * (-chromDiff.toDouble()) / 12.0).toFloat()
            Logger.d(
                TAG,
                "AutoMix Key: whole tone shift $chromDiff semitones, ratio=${"%.4f".format(pitchRatio)}",
            )
            return pitchRatio
        }

        // Distance >= 3 or cross-type distance 2: too far, shifting would sound unnatural
        Logger.d(TAG, "AutoMix Key: dist=$dist too far apart, no shift")
        return 1.0f
    }

    /**
     * Map a musical key name to its chromatic semitone number (0-11).
     * C=0, C#/Db=1, D=2, ..., B=11. Returns -1 for unknown keys.
     */
    private fun keyToSemitone(key: String): Int =
        when (key.trim()) {
            "C" -> 0
            "C#", "Db" -> 1
            "D" -> 2
            "D#", "Eb" -> 3
            "E" -> 4
            "F" -> 5
            "F#", "Gb" -> 6
            "G" -> 7
            "G#", "Ab" -> 8
            "A" -> 9
            "A#", "Bb" -> 10
            "B" -> 11
            else -> -1
        }

    companion object {
        // DJ crossfade sigmoid steepness (higher = sharper S-curve transition)
        private const val DJ_FILTER_SIGMOID_K = 6f

        // DJ crossfade filter frequency bounds
        private const val LPF_START_HZ = 20000f // Low-pass starts wide open
        private const val LPF_END_HZ = 200f // Low-pass ends muffled (keeps bass thump like Pioneer DJM)
        private const val HPF_START_HZ = 2000f // High-pass starts lower — incoming track fills in faster
        private const val HPF_END_HZ = 20f // High-pass ends wide open

        // AutoMix constants
        private const val AUTO_FALLBACK_DURATION_MS = 25000 // Default when no BPM data
        private const val AUTO_MIN_DURATION_MS = 15000
        private const val AUTO_MAX_DURATION_MS = 40000
        private val BEAT_COUNT_OPTIONS = intArrayOf(16, 32, 48, 64, 80, 96)
        private const val DEFAULT_BEAT_COUNT = 32
        private const val BPM_RATIO_MIN = 0.5f // Max 50% slower
        private const val BPM_RATIO_MAX = 1.5f // Max 50% faster

        // Quantization step for speed/pitch ramp (0.5% = 0.005)
        // Prevents SonicAudioProcessor from popping on micro-adjustments
        private const val SPEED_PITCH_STEP = 0.02f
    }

    /**
     * Finalize crossfade: swap players and cleanup.
     * Mirrors GstreamerPlayerAdapter.finalizeCrossfade()
     */
    private fun finalizeCrossfade(
        nextIndex: Int,
        nextPlayer: ExoPlayer,
    ) {
        Logger.d(TAG, "Crossfade complete, swapping players")

        // Cleanup old current player WITHOUT touching listeners
        // (listeners are already setup for nextPlayer via setupPlayerListenerInternal)
        stopPositionUpdates()

        // Cleanup the old current player manually
        currentPlayer?.let { oldPlayer ->
            try {
                oldPlayer.stop()
                oldPlayer.release()
            } catch (e: Exception) {
                Logger.w(TAG, "Error cleaning up old player: ${e.message}")
            }
        }

        // Disable and reset DJ filters on the new current player (no overhead during normal playback)
        secondaryPlayerFilter?.let { filter ->
            filter.enabled = false
        }
        // Old player's filter is released with the old player (GC'd)

        // Promote secondary to current
        currentPlayer = nextPlayer
        currentPlayerFilter = secondaryPlayerFilter
        secondaryPlayer = null
        secondaryPlayerFilter = null
        // localCurrentMediaItemIndex already updated in triggerCrossfadeTransition()

        // Enable audio focus and headphone-disconnect handling on new current player
        nextPlayer.setAudioAttributes(audioAttributes, true)
        nextPlayer.setHandleAudioBecomingNoisy(true)

        // Ensure correct volume and playback parameters
        currentPlayer?.volume = internalVolume
        currentPlayer?.playbackParameters = PlaybackParameters(internalPlaybackSpeed, internalPlaybackPitch)
        currentPlayer?.skipSilenceEnabled = internalSkipSilence

        // Reset state
        setCrossfading(false)
        crossfadeFromIndex = -1
        transitionToState(InternalState.PLAYING)

        // Ensure MediaSession notification has correct metadata after crossfade completes.
        // setAudioAttributes() above may cause a brief audio session reset, so re-notify
        // MediaSession to guarantee the notification shows the correct track info.
        forwardingPlayer.notifyMediaItemChanged()

        // Start position tracking
        startPositionUpdates()

        // Trigger next precache
        triggerPrecachingInternal()
    }

    // ========== Internal: Position Updates ==========

    /**
     * Start position updates (periodic background task).
     * Also handles crossfade detection when position approaches end.
     */
    private fun startPositionUpdates() {
        stopPositionUpdates()

        positionUpdateJob =
            coroutineScope.launch {
                while (isActive && currentPlayer != null) {
                    try {
                        currentPlayer?.let { player ->
                            if (internalState == InternalState.PLAYING ||
                                internalState == InternalState.READY ||
                                internalState == InternalState.PAUSED
                            ) {
                                // During crossfade, show the incoming track's timeline
                                val timelinePlayer = if (isCrossfading) secondaryPlayer ?: player else player
                                val pos = timelinePlayer.currentPosition
                                val dur = timelinePlayer.duration
                                val buf = timelinePlayer.bufferedPosition

                                if (pos >= 0) cachedPosition = pos
                                if (dur > 0) cachedDuration = dur
                                if (buf >= 0) cachedBufferedPosition = buf

                                // Check if should trigger crossfade.
                                // Add a preparation buffer (3s) so that if the next track
                                // is NOT precached, URL resolution + buffering time doesn't
                                // eat into the audible crossfade window.
                                if (crossfadeEnabled &&
                                    !isCrossfading &&
                                    dur > 0 &&
                                    pos > 0
                                ) {
                                    // Account for playback speed: at higher speed, media time
                                    // is consumed faster, so wall-clock remaining is shorter
                                    val speed = internalPlaybackSpeed.coerceAtLeast(0.1f)
                                    val timeRemaining = ((dur - pos) / speed).toLong()
                                    val nextVideoId = playlist.getOrNull(getNextMediaItemIndex())?.mediaId
                                    val isPrecached = nextVideoId != null && precachedPlayers.containsKey(nextVideoId)
                                    // If next track is precached, trigger at exactly crossfadeDurationMs.
                                    // If NOT precached, trigger 3s earlier to allow preparation time.
                                    val preparationBufferMs = if (isPrecached) 0L else 3000L
                                    // In Auto mode, calculate beat-quantized duration for trigger threshold.
                                    val resolvedDurationMs =
                                        if (crossfadeDurationMs == DataStoreManager.CROSSFADE_DURATION_AUTO) {
                                            val currentVideoId = playlist.getOrNull(localCurrentMediaItemIndex)?.mediaId ?: ""
                                            resolveAutoCrossfadeDurationMs(currentVideoId, nextVideoId ?: "")
                                        } else {
                                            crossfadeDurationMs
                                        }
                                    val triggerThreshold = resolvedDurationMs.toLong() + preparationBufferMs
                                    if (timeRemaining in 1..triggerThreshold) {
                                        if (hasNextMediaItem()) {
                                            val nextIndex = getNextMediaItemIndex()
                                            triggerCrossfadeTransition(nextIndex)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore query errors - don't log to avoid spam
                    }

                    delay(200) // Update every 200ms
                }
            }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    // ========== Internal: Precaching ==========

    /**
     * Trigger precaching - mirrors GstreamerPlayerAdapter.triggerPrecachingInternal()
     *
     * Creates ExoPlayer instances for upcoming tracks. Each player gets a MediaItem
     * and calls prepare(), which triggers URL resolution and buffering via MediaSourceFactory.
     */
    private fun triggerPrecachingInternal() {
        if (!precacheEnabled || playlist.isEmpty()) return

        cancelPrecaching()
        Logger.d(TAG, "Trigger precache")
        precacheJob =
            coroutineScope.launch {
                try {
                    val indicesToPrecache = mutableListOf<Int>()

                    val index = localCurrentMediaItemIndex
                    for (i in 1..maxPrecacheCount) {
                        val nextIndex =
                            when (internalRepeatMode) {
                                PlayerConstants.REPEAT_MODE_ALL -> {
                                    (index + i) % playlist.size
                                }
                                else -> {
                                    val next = index + i
                                    if (next < playlist.size) next else break
                                }
                            }

                        if (nextIndex != localCurrentMediaItemIndex &&
                            !precachedPlayers.containsKey(playlist.getOrNull(nextIndex)?.mediaId)
                        ) {
                            indicesToPrecache.add(nextIndex)
                        }
                    }

                    for (idx in indicesToPrecache) {
                        if (!isActive) break

                        val mediaItem = playlist.getOrNull(idx) ?: continue

                        try {
                            val pwf = createExoPlayerInstance(handleAudioFocus = false)
                            pwf.player.setMediaItem(mediaItem.toMedia3MediaItem())
                            pwf.player.prepare()
                            precachedPlayers[mediaItem.mediaId] = PrecachedPlayer(pwf.player, mediaItem, pwf.filter)
                            Logger.d(TAG, "Precached player for index $idx")
                        } catch (e: Exception) {
                            Logger.e(TAG, "Precaching error for $idx: ${e.message}")
                        }

                        delay(100)
                    }
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        Logger.e(TAG, "Precaching error: ${e.message}")
                    }
                }
            }
    }

    private fun cancelPrecaching() {
        precacheJob?.cancel()
        precacheJob = null
    }

    private fun clearPrecacheExceptCurrentInternal() {
        Logger.d(TAG, "Clearing precache")
        precachedPlayers.entries.removeIf { (videoId, cached) ->
            if (videoId != currentMediaItem?.mediaId) {
                cleanupPlayerInternal(cached.player)
                true
            } else {
                false
            }
        }
    }

    private fun clearAllPrecacheInternal() {
        Logger.d(TAG, "Clearing all precache")
        precachedPlayers.values.forEach { cleanupPlayerInternal(it.player) }
        precachedPlayers.clear()
    }

    // ========== Internal: Notifications ==========

    private fun notifyEqualizerIntent(shouldOpen: Boolean) {
        listeners.forEach { it.shouldOpenOrCloseEqualizerIntent(shouldOpen) }
    }

    // ========== Internal: Shuffle Management ==========
    // Mirrors GstreamerPlayerAdapter shuffle management exactly

    private fun createShuffleOrder() {
        if (playlist.isEmpty()) {
            shuffleIndices.clear()
            shuffleOrder.clear()
            return
        }

        val indices = playlist.indices.toMutableList()

        val currentIndex = localCurrentMediaItemIndex
        if (currentIndex in indices) {
            indices.removeAt(currentIndex)
        }

        indices.shuffle()

        if (currentIndex in playlist.indices) {
            indices.add(0, currentIndex)
        }

        shuffleOrder.clear()
        shuffleOrder.addAll(indices)

        shuffleIndices.clear()
        shuffleIndices.addAll(List(playlist.size) { 0 })
        shuffleOrder.forEachIndexed { shuffledPos, originalIndex ->
            shuffleIndices[originalIndex] = shuffledPos
        }

        Logger.d(TAG, "Created shuffle order: $shuffleOrder")
    }

    private fun clearShuffleOrder() {
        shuffleIndices.clear()
        shuffleOrder.clear()
        Logger.d(TAG, "Cleared shuffle order")
    }

    private fun insertIntoShuffleOrder(
        insertedOriginalIndex: Int,
        afterShufflePos: Int,
    ) {
        if (playlist.isEmpty() || insertedOriginalIndex !in playlist.indices) {
            return
        }

        for (i in shuffleOrder.indices) {
            if (shuffleOrder[i] >= insertedOriginalIndex) {
                shuffleOrder[i]++
            }
        }

        val insertPos = (afterShufflePos + 1).coerceIn(0, shuffleOrder.size)
        shuffleOrder.add(insertPos, insertedOriginalIndex)

        shuffleIndices.clear()
        shuffleIndices.addAll(List(playlist.size) { 0 })
        shuffleOrder.forEachIndexed { shuffledPos, origIndex ->
            if (origIndex < shuffleIndices.size) {
                shuffleIndices[origIndex] = shuffledPos
            }
        }

        Logger.d(
            TAG,
            "Inserted index $insertedOriginalIndex into shuffle at position $insertPos (after shuffle pos $afterShufflePos)",
        )
    }

    private fun getShuffledMediaItemList(): List<GenericMediaItem> {
        if (!internalShuffleModeEnabled || shuffleOrder.isEmpty()) {
            return playlist.toList()
        }
        return shuffleOrder.mapNotNull { playlist.getOrNull(it) }
    }

    private fun notifyTimelineChanged(reason: String) {
        val list = getShuffledMediaItemList()
        listeners.forEach { it.onTimelineChanged(list, reason) }
    }
}