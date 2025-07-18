package com.abcg.music.viewModel

import android.app.Application
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.maxrave.kotlinytmusicscraper.models.response.DownloadProgress
import com.maxrave.kotlinytmusicscraper.models.simpmusic.GithubResponse
import com.abcg.music.R
import com.abcg.music.common.Config.ALBUM_CLICK
import com.abcg.music.common.Config.DOWNLOAD_CACHE
import com.abcg.music.common.Config.PLAYLIST_CLICK
import com.abcg.music.common.Config.RECOVER_TRACK_QUEUE
import com.abcg.music.common.Config.SHARE
import com.abcg.music.common.Config.SONG_CLICK
import com.abcg.music.common.Config.VIDEO_CLICK
import com.abcg.music.common.DownloadState
import com.abcg.music.common.SELECTED_LANGUAGE
import com.abcg.music.common.STATUS_DONE
import com.abcg.music.data.dataStore.DataStoreManager
import com.abcg.music.data.dataStore.DataStoreManager.Settings.FALSE
import com.abcg.music.data.dataStore.DataStoreManager.Settings.TRUE
import com.abcg.music.data.db.entities.AlbumEntity
import com.abcg.music.data.db.entities.LocalPlaylistEntity
import com.abcg.music.data.db.entities.LyricsEntity
import com.abcg.music.data.db.entities.NewFormatEntity
import com.abcg.music.data.db.entities.PairSongLocalPlaylist
import com.abcg.music.data.db.entities.PlaylistEntity
import com.abcg.music.data.db.entities.SongEntity
import com.abcg.music.data.db.entities.SongInfoEntity
import com.abcg.music.data.db.entities.TranslatedLyricsEntity
import com.abcg.music.data.model.browse.album.Track
import com.abcg.music.data.model.metadata.Line
import com.abcg.music.data.model.metadata.Lyrics
import com.abcg.music.extension.connectArtists
import com.abcg.music.extension.isSong
import com.abcg.music.extension.isVideo
import com.abcg.music.extension.toArrayListTrack
import com.abcg.music.extension.toListName
import com.abcg.music.extension.toLyrics
import com.abcg.music.extension.toLyricsEntity
import com.abcg.music.extension.toMediaItem
import com.abcg.music.extension.toSongEntity
import com.abcg.music.extension.toTrack
import com.abcg.music.service.ControlState
import com.abcg.music.service.NowPlayingTrackState
import com.abcg.music.service.PlayerEvent
import com.abcg.music.service.PlaylistType
import com.abcg.music.service.QueueData
import com.abcg.music.service.RepeatState
import com.abcg.music.service.SimpleMediaState
import com.abcg.music.service.SleepTimerState
import com.abcg.music.service.test.download.DownloadUtils
import com.abcg.music.service.test.notification.NotifyWork
import com.abcg.music.utils.Resource
import com.abcg.music.utils.VersionManager
import com.abcg.music.viewModel.base.BaseViewModel
import com.maxrave.spotify.model.response.spotify.CanvasResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@UnstableApi
class SharedViewModel(
    private val application: Application,
) : BaseViewModel(application) {
    var isFirstLiked: Boolean = false
    var isFirstMiniplayer: Boolean = false
    var isFirstSuggestions: Boolean = false
    var showedUpdateDialog: Boolean = false
    var showOrHideMiniplayer: MutableSharedFlow<Boolean> = MutableSharedFlow()
    private val _showMusixmatchCaptchaWebView: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showMusixmatchCaptchaWebView: StateFlow<Boolean> get() = _showMusixmatchCaptchaWebView

    private val downloadedCache: SimpleCache by inject(qualifier = named(DOWNLOAD_CACHE))

    private val downloadUtils: DownloadUtils by inject()

    private var _songDB: MutableLiveData<SongEntity?> = MutableLiveData()
    val songDB: LiveData<SongEntity?> = _songDB
    private var _liked: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val liked: SharedFlow<Boolean> = _liked.asSharedFlow()

    private var _downloadList: MutableStateFlow<ArrayList<SongEntity>?> = MutableStateFlow(null)
    val downloadList: SharedFlow<ArrayList<SongEntity>?> = _downloadList.asSharedFlow()

    private val context
        get() = getApplication<Application>()

    val isServiceRunning = MutableLiveData<Boolean>(false)

    var videoId = MutableLiveData<String>()

    var gradientDrawable: MutableLiveData<GradientDrawable> = MutableLiveData()

    var _lyrics = MutableStateFlow<Resource<Lyrics>?>(null)

    //    val lyrics: LiveData<Resource<Lyrics>> = _lyrics
    private var lyricsFormat: MutableLiveData<ArrayList<Line>> = MutableLiveData()
    var lyricsFull = MutableLiveData<String>()

    private var _sleepTimerState = MutableStateFlow(SleepTimerState(false, 0))
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState

    private var regionCode: String? = null
    private var language: String? = null
    private var quality: String? = null

    private var _format: MutableStateFlow<NewFormatEntity?> = MutableStateFlow(null)
    val format: SharedFlow<NewFormatEntity?> = _format.asSharedFlow()

    private var _canvas: MutableStateFlow<CanvasResponse?> = MutableStateFlow(null)
    val canvas: StateFlow<CanvasResponse?> = _canvas

    private var canvasJob: Job? = null

    val intent: MutableStateFlow<Intent?> = MutableStateFlow(null)

    private var getFormatFlowJob: Job? = null

    var playlistId: MutableStateFlow<String?> = MutableStateFlow(null)

    var isFullScreen: Boolean = false

    private var _nowPlayingState = MutableStateFlow<NowPlayingTrackState?>(null)
    val nowPlayingState: StateFlow<NowPlayingTrackState?> = _nowPlayingState

    val blurBg: StateFlow<Boolean> =
        dataStoreManager.blurPlayerBackground
            .map { it == TRUE }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(500L),
                initialValue = false,
            )

    private var _controllerState =
        MutableStateFlow<ControlState>(
            ControlState(
                isPlaying = false,
                isShuffle = false,
                repeatState = RepeatState.None,
                isLiked = false,
                isNextAvailable = false,
                isPreviousAvailable = false,
            ),
        )
    val controllerState: StateFlow<ControlState> = _controllerState
    private val _getVideo: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val getVideo: StateFlow<Boolean> = _getVideo

    private var _timeline =
        MutableStateFlow<TimeLine>(
            TimeLine(
                current = -1L,
                total = -1L,
                bufferedPercent = 0,
                loading = true,
            ),
        )
    val timeline: StateFlow<TimeLine> = _timeline

    private var _nowPlayingScreenData =
        MutableStateFlow<NowPlayingScreenData>(
            NowPlayingScreenData.initial(),
        )
    val nowPlayingScreenData: StateFlow<NowPlayingScreenData> = _nowPlayingScreenData

    private var _likeStatus = MutableStateFlow<Boolean>(false)
    val likeStatus: StateFlow<Boolean> = _likeStatus

    val openAppTime: StateFlow<Int> = dataStoreManager.openAppTime.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    init {
        mainRepository.initYouTube(viewModelScope)
        viewModelScope.launch {
            log("SharedViewModel init")
            if (dataStoreManager.appVersion.first() != VersionManager.getVersionName()) {
                dataStoreManager.resetOpenAppTime()
                dataStoreManager.setAppVersion(
                    VersionManager.getVersionName(),
                )
            }
            dataStoreManager.openApp()
            val timeLineJob =
                launch {
                    combine(
                        timeline.filterNotNull(),
                        nowPlayingState.filterNotNull()
                    ) { timeline, nowPlayingState ->
                        Pair(timeline, nowPlayingState)
                    }.distinctUntilChanged { old, new ->
                        (old.first.total.toString() + old.second?.songEntity?.videoId).hashCode() ==
                            (new.first.total.toString() + new.second?.songEntity?.videoId).hashCode()
                    }
                        .collectLatest {
                            log("Timeline job ${(it.first.total.toString() + it.second?.songEntity?.videoId).hashCode()}")
                        val nowPlaying = it.second
                        val timeline = it.first
                        if (timeline.total > 0 && nowPlaying?.songEntity != null) {
                            if (nowPlaying.mediaItem.isSong() && nowPlayingScreenData.value.canvasData == null) {
                                Log.w(tag, "Duration is ${timeline.total}")
                                Log.w(tag, "MediaId is ${nowPlaying.mediaItem.mediaId}")
                                getCanvas(nowPlaying.mediaItem.mediaId, (timeline.total / 1000).toInt())
                            }
                            nowPlaying.songEntity.let { song ->
                                if (nowPlayingScreenData.value.lyricsData == null) {
                                    Log.w(tag, "Get lyrics from format")
                                    getLyricsFromFormat(song, (timeline.total / 1000).toInt())
                                }
                            }
                        }
                    }
                }

            val downloadedJob =
                launch {
                    mainRepository
                        .getDownloadedSongsAsFlow(offset = 0)
                        .distinctUntilChanged()
                        .collectLatest {
                            _downloadList.value = it?.toCollection(ArrayList())
                        }
                }

            val checkGetVideoJob =
                launch {
                    dataStoreManager.watchVideoInsteadOfPlayingAudio.collectLatest {
                        Log.w(tag, "GetVideo is $it")
                        _getVideo.value = it == TRUE
                    }
                }
            val lyricsProviderJob =
                launch {
                    dataStoreManager.lyricsProvider.distinctUntilChanged().collectLatest {
                        setLyricsProvider()
                    }
                }
            timeLineJob.join()
            downloadedJob.join()
            checkGetVideoJob.join()
            lyricsProviderJob.join()
        }
    }

    init {
        runBlocking {
            dataStoreManager.getString("miniplayer_guide").first().let {
                isFirstMiniplayer = it != STATUS_DONE
            }
            dataStoreManager.getString("suggest_guide").first().let {
                isFirstSuggestions = it != STATUS_DONE
            }
            dataStoreManager.getString("liked_guide").first().let {
                isFirstLiked = it != STATUS_DONE
            }
        }
        viewModelScope.launch {
            simpleMediaServiceHandler.nowPlayingState
                .distinctUntilChangedBy {
                    it.songEntity?.videoId
                }.collectLatest { state ->
                    Log.w(tag, "NowPlayingState is $state")
                    canvasJob?.cancel()
                    _nowPlayingState.value = state
                    state.track?.let { track ->
                        _nowPlayingScreenData.value =
                            NowPlayingScreenData(
                                nowPlayingTitle = state.track.title,
                                artistName =
                                    state.track
                                        .artists
                                        .toListName()
                                        .joinToString(", "),
                                isVideo = false,
                                thumbnailURL = null,
                                canvasData = null,
                                lyricsData = null,
                                songInfoData = null,
                                playlistName = simpleMediaServiceHandler.queueData.value?.playlistName ?: "",
                            )
                    }
                    state.mediaItem.let { now ->
                        _canvas.value = null
                        getLikeStatus(now.mediaId)
                        getSongInfo(now.mediaId)
                        getFormat(now.mediaId)
                        _nowPlayingScreenData.update {
                            it.copy(
                                isVideo = now.isVideo(),
                            )
                        }
                    }
                    state.songEntity?.let { song ->
                        _liked.value = song.liked == true
                        _nowPlayingScreenData.update {
                            it.copy(
                                thumbnailURL = song.thumbnails,
                                isExplicit = song.isExplicit,
                            )
                        }
                    }
                }
        }
        viewModelScope.launch {
            val job1 =
                launch {
                    simpleMediaServiceHandler.simpleMediaState.collect { mediaState ->
                        when (mediaState) {
                            is SimpleMediaState.Buffering -> {
                                _timeline.update {
                                    it.copy(
                                        loading = true,
                                    )
                                }
                            }

                            SimpleMediaState.Initial -> {
                                _timeline.update { it.copy(loading = true) }
                            }
                            SimpleMediaState.Ended -> {
                                _timeline.update {
                                    it.copy(
                                        current = -1L,
                                        total = -1L,
                                        bufferedPercent = 0,
                                        loading = true,
                                    )
                                }
                            }

                            is SimpleMediaState.Progress -> {
                                if (mediaState.progress >= 0L && mediaState.progress != _timeline.value.current) {
                                    if (_timeline.value.total > 0L) {
                                        _timeline.update {
                                            it.copy(
                                                current = mediaState.progress,
                                                loading = false,
                                            )
                                        }
                                    } else {
                                        _timeline.update {
                                            it.copy(
                                                current = mediaState.progress,
                                                loading = true,
                                                total = simpleMediaServiceHandler.getPlayerDuration(),
                                            )
                                        }
                                    }
                                } else {
                                    _timeline.update {
                                        it.copy(
                                            loading = true,
                                        )
                                    }
                                }
                            }

                            is SimpleMediaState.Loading -> {
                                _timeline.update {
                                    it.copy(
                                        bufferedPercent = mediaState.bufferedPercentage,
                                        total = mediaState.duration,
                                    )
                                }
                            }

                            is SimpleMediaState.Ready -> {
                                _timeline.update {
                                    it.copy(
                                        current = simpleMediaServiceHandler.getProgress(),
                                        loading = false,
                                        total = mediaState.duration,
                                    )
                                }
                            }
                        }
                    }
                }
            val controllerJob =
                launch {
                    Log.w(tag, "ControllerJob is running")
                    simpleMediaServiceHandler.controlState.collectLatest {
                        Log.w(tag, "ControlState is $it")
                        _controllerState.value = it
                    }
                }
            val sleepTimerJob =
                launch {
                    simpleMediaServiceHandler.sleepTimerState.collectLatest {
                        _sleepTimerState.value = it
                    }
                }
            val playlistNameJob =
                launch {
                    simpleMediaServiceHandler.queueData.collectLatest {
                        _nowPlayingScreenData.update {
                            it.copy(playlistName = it.playlistName)
                        }
                    }
                }
            job1.join()
            controllerJob.join()
            sleepTimerJob.join()
            playlistNameJob.join()
        }
    }

    fun blurFullscreenLyrics(): Boolean = runBlocking { dataStoreManager.blurFullscreenLyrics.first() == TRUE }

    private fun getLikeStatus(videoId: String?) {
        viewModelScope.launch {
            if (videoId != null) {
                _likeStatus.value = false
                mainRepository.getLikeStatus(videoId).collectLatest { status ->
                    _likeStatus.value = status
                }
            }
        }
    }

    fun resolvedMusixmatchCaptcha() {
        _showMusixmatchCaptchaWebView.value = false
    }

    fun getMusixmatchCookie() = runBlocking { dataStoreManager.musixmatchCookie.first() }

    private fun getCanvas(
        videoId: String,
        duration: Int,
    ) {
        Log.w(tag, "Start getCanvas: $videoId $duration")
//        canvasJob?.cancel()
        viewModelScope.launch {
            if (dataStoreManager.spotifyCanvas.first() == TRUE) {
                mainRepository.getCanvas(videoId, duration).cancellable().collect { response ->
                    _canvas.value = response
                    Log.w(tag, "Canvas is $response")
                    if (response != null && nowPlayingState.value?.mediaItem?.mediaId == videoId) {
                        _nowPlayingScreenData.update {
                            it.copy(
                                canvasData =
                                    response.canvases.firstOrNull()?.canvas_url?.let { canvasUrl ->
                                        NowPlayingScreenData.CanvasData(
                                            isVideo = canvasUrl.contains(".mp4"),
                                            url = canvasUrl,
                                        )
                                    },
                            )
                        }
                        if (response
                                .canvases
                                .firstOrNull()
                                ?.canvas_url
                                ?.contains(".mp4") == true
                        ) {
                            mainRepository.updateCanvasUrl(videoId, response.canvases.first().canvas_url)
                        }
                        val canvasThumbs = response.canvases.firstOrNull()?.thumbsOfCanva
                        if (!canvasThumbs.isNullOrEmpty()) {
                            (canvasThumbs.let {
                                it.maxByOrNull {
                                    (it.height ?: 0) + (it.width ?: 0)
                                }?.url
                            } ?: canvasThumbs.first().url)?.let { thumb ->
                                mainRepository.updateCanvasThumbUrl(videoId, thumb)
                            }
                        }
                    } else {
                        nowPlayingState.value?.songEntity?.canvasUrl?.let { url ->
                            _nowPlayingScreenData.update {
                                it.copy(
                                    canvasData =
                                        NowPlayingScreenData.CanvasData(
                                            isVideo = url.contains(".mp4"),
                                            url = url,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun getString(key: String): String? = runBlocking { dataStoreManager.getString(key).first() }

    fun putString(
        key: String,
        value: String,
    ) {
        runBlocking { dataStoreManager.putString(key, value) }
    }

    fun setSleepTimer(minutes: Int) {
        simpleMediaServiceHandler.sleepStart(minutes)
    }

    fun stopSleepTimer() {
        simpleMediaServiceHandler.sleepStop()
    }

    private var _downloadState: MutableStateFlow<Download?> = MutableStateFlow(null)
    var downloadState: StateFlow<Download?> = _downloadState.asStateFlow()

    fun getDownloadStateFromService(videoId: String) {
    }

    fun checkIsRestoring() {
        viewModelScope.launch {
            mainRepository.getDownloadedSongs().first().let { songs ->
                songs?.forEach { song ->
                    if (!downloadedCache.keys.contains(song.videoId)) {
                        mainRepository.updateDownloadState(
                            song.videoId,
                            DownloadState.STATE_NOT_DOWNLOADED,
                        )
                    }
                }
            }
            mainRepository.getAllDownloadedPlaylist().first().let { list ->
                for (data in list) {
                    when (data) {
                        is AlbumEntity -> {
                            if (data.tracks.isNullOrEmpty() ||
                                (
                                    !downloadedCache.keys.containsAll(
                                        data.tracks,
                                    )
                                )
                            ) {
                                mainRepository.updateAlbumDownloadState(
                                    data.browseId,
                                    DownloadState.STATE_NOT_DOWNLOADED,
                                )
                            }
                        }

                        is PlaylistEntity -> {
                            if (data.tracks.isNullOrEmpty() ||
                                (
                                    !downloadedCache.keys.containsAll(
                                        data.tracks,
                                    )
                                )
                            ) {
                                mainRepository.updatePlaylistDownloadState(
                                    data.id,
                                    DownloadState.STATE_NOT_DOWNLOADED,
                                )
                            }
                        }

                        is LocalPlaylistEntity -> {
                            if (data.tracks.isNullOrEmpty() ||
                                (
                                    !downloadedCache.keys.containsAll(
                                        data.tracks,
                                    )
                                )
                            ) {
                                mainRepository.updateLocalPlaylistDownloadState(
                                    DownloadState.STATE_NOT_DOWNLOADED,
                                    data.id,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun insertLyrics(lyrics: LyricsEntity) {
        viewModelScope.launch {
            mainRepository.insertLyrics(lyrics)
        }
    }

    private fun getSavedLyrics(track: Track) {
        viewModelScope.launch {
            resetLyrics()
            mainRepository.getSavedLyrics(track.videoId).cancellable().collectLatest { lyrics ->
                if (lyrics != null) {
                    val lyricsData = lyrics.toLyrics()
                    Log.d(tag, "Saved Lyrics $lyricsData")
                    updateLyrics(
                        track.videoId,
                        lyricsData,
                        false,
                        LyricsProvider.OFFLINE,
                    )
                    getAITranslationLyrics(
                        track.videoId,
                        lyricsData
                    )
                }
            }
        }
    }

    fun getCurrentMediaItemIndex(): Int = runBlocking { simpleMediaServiceHandler.currentSongIndex.first() }

    @UnstableApi
    fun playMediaItemInMediaSource(index: Int) {
        simpleMediaServiceHandler.playMediaItemInMediaSource(index)
    }

    fun loadSharedMediaItem(videoId: String) {
        viewModelScope.launch {
            mainRepository.getFullMetadata(videoId).collectLatest {
                if (it != null) {
                    val track = it.toTrack()
                    simpleMediaServiceHandler.setQueueData(
                        QueueData(
                            listTracks = arrayListOf(track),
                            firstPlayedTrack = track,
                            playlistId = "RDAMVM$videoId",
                            playlistName = context.getString(R.string.shared),
                            playlistType = PlaylistType.RADIO,
                            continuation = null,
                        ),
                    )
                    loadMediaItemFromTrack(track, SONG_CLICK)
                } else {
                    Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @UnstableApi
    fun loadMediaItemFromTrack(
        track: Track,
        type: String,
        index: Int? = null,
    ) {
        quality = runBlocking { dataStoreManager.quality.first() }
        viewModelScope.launch {
            simpleMediaServiceHandler.clearMediaItems()
            mainRepository.insertSong(track.toSongEntity()).first().let {
                println("insertSong: $it")
                mainRepository
                    .getSongById(track.videoId)
                    .collect { songEntity ->
                        _songDB.value = songEntity
                        if (songEntity != null) {
                            Log.w("Check like", "loadMediaItemFromTrack ${songEntity.liked}")
                            _liked.value = songEntity.liked
                        }
                    }
            }
            track.durationSeconds?.let {
                mainRepository.updateDurationSeconds(
                    it,
                    track.videoId,
                )
            }
            withContext(Dispatchers.Main) {
                simpleMediaServiceHandler.addMediaItem(track.toMediaItem(), playWhenReady = type != RECOVER_TRACK_QUEUE)
            }

            when (type) {
                SONG_CLICK -> {
                    simpleMediaServiceHandler.getRelated(track.videoId)
                }

                VIDEO_CLICK -> {
                    simpleMediaServiceHandler.getRelated(track.videoId)
                }

                SHARE -> {
                    simpleMediaServiceHandler.getRelated(track.videoId)
                }

                PLAYLIST_CLICK -> {
                    if (index == null) {
//                                        fetchSourceFromQueue(downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = 0)
                    } else {
//                                        fetchSourceFromQueue(index!!, downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = index)
                    }
                }

                ALBUM_CLICK -> {
                    if (index == null) {
//                                        fetchSourceFromQueue(downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = 0)
                    } else {
//                                        fetchSourceFromQueue(index!!, downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = index)
                    }
                }
            }
        }
    }

    @UnstableApi
    fun onUIEvent(uiEvent: UIEvent) =
        viewModelScope.launch {
            when (uiEvent) {
                UIEvent.Backward ->
                    simpleMediaServiceHandler.onPlayerEvent(
                        PlayerEvent.Backward,
                    )

                UIEvent.Forward -> simpleMediaServiceHandler.onPlayerEvent(PlayerEvent.Forward)
                UIEvent.PlayPause ->
                    simpleMediaServiceHandler.onPlayerEvent(
                        PlayerEvent.PlayPause,
                    )

                UIEvent.Next -> simpleMediaServiceHandler.onPlayerEvent(PlayerEvent.Next)
                UIEvent.Previous ->
                    simpleMediaServiceHandler.onPlayerEvent(
                        PlayerEvent.Previous,
                    )

                UIEvent.Stop -> simpleMediaServiceHandler.onPlayerEvent(PlayerEvent.Stop)
                is UIEvent.UpdateProgress -> {
                    simpleMediaServiceHandler.onPlayerEvent(
                        PlayerEvent.UpdateProgress(
                            uiEvent.newProgress,
                        ),
                    )
                }

                UIEvent.Repeat -> simpleMediaServiceHandler.onPlayerEvent(PlayerEvent.Repeat)
                UIEvent.Shuffle -> simpleMediaServiceHandler.onPlayerEvent(PlayerEvent.Shuffle)
                UIEvent.ToggleLike -> {
                    Log.w(tag, "ToggleLike")
                    simpleMediaServiceHandler.onPlayerEvent(PlayerEvent.ToggleLike)
                }
            }
        }

    private var _localPlaylist: MutableStateFlow<List<LocalPlaylistEntity>> = MutableStateFlow(listOf())
    val localPlaylist: StateFlow<List<LocalPlaylistEntity>> = _localPlaylist

    fun getAllLocalPlaylist() {
        viewModelScope.launch {
            mainRepository.getAllLocalPlaylists().collect { values ->
                _localPlaylist.emit(values)
            }
        }
    }

    fun updateLocalPlaylistTracks(
        list: List<String>,
        id: Long,
    ) {
        viewModelScope.launch {
            mainRepository.getSongsByListVideoId(list).collect { values ->
                var count = 0
                values.forEach { song ->
                    if (song.downloadState == DownloadState.STATE_DOWNLOADED) {
                        count++
                    }
                }
                mainRepository.updateLocalPlaylistTracks(list, id)
                Toast
                    .makeText(
                        getApplication(),
                        application.getString(R.string.added_to_playlist),
                        Toast.LENGTH_SHORT,
                    ).show()
                if (count == values.size) {
                    mainRepository.updateLocalPlaylistDownloadState(
                        DownloadState.STATE_DOWNLOADED,
                        id,
                    )
                } else {
                    mainRepository.updateLocalPlaylistDownloadState(
                        DownloadState.STATE_NOT_DOWNLOADED,
                        id,
                    )
                }
            }
        }
    }

    fun getActiveLyrics(current: Long): Int? {
        val lyricsFormat = _lyrics.value?.data?.lines
        lyricsFormat?.indices?.forEach { i ->
            val sentence = lyricsFormat[i]
            val startTimeMs = sentence.startTimeMs.toLong()

            // estimate the end time of the current sentence based on the start time of the next sentence
            val endTimeMs =
                if (i < lyricsFormat.size - 1) {
                    lyricsFormat[i + 1].startTimeMs.toLong()
                } else {
                    // if this is the last sentence, set the end time to be some default value (e.g., 1 minute after the start time)
                    startTimeMs + 60000
                }
            if (current in startTimeMs..endTimeMs) {
                return i
            }
        }
        if (!lyricsFormat.isNullOrEmpty() &&
            (
                current in (
                    0..(
                        lyricsFormat.getOrNull(0)?.startTimeMs
                            ?: "0"
                    ).toLong()
                )
            )
        ) {
            return -1
        }
        return null
    }

    @UnstableApi
    override fun onCleared() {
        Log.w("Check onCleared", "onCleared")
    }

    private fun resetLyrics() {
        _lyrics.value = (Resource.Error<Lyrics>("reset"))
        lyricsFormat.postValue(arrayListOf())
        lyricsFull.postValue("")
    }

    fun updateDownloadState(
        videoId: String,
        state: Int,
    ) {
        viewModelScope.launch {
            mainRepository.getSongById(videoId).collect { songEntity ->
                _songDB.value = songEntity
                if (songEntity != null) {
                    Log.w(
                        "Check like",
                        "SharedViewModel updateDownloadState ${songEntity.liked}",
                    )
                    _liked.value = songEntity.liked
                }
            }
            mainRepository.updateDownloadState(videoId, state)
        }
    }

    fun changeAllDownloadingToError() {
        viewModelScope.launch {
            mainRepository.getDownloadingSongs().collect { songs ->
                songs?.forEach { song ->
                    mainRepository.updateDownloadState(
                        song.videoId,
                        DownloadState.STATE_NOT_DOWNLOADED,
                    )
                }
            }
        }
    }

//    val _artistId: MutableLiveData<Resource<ChannelId>> = MutableLiveData()
//    var artistId: LiveData<Resource<ChannelId>> = _artistId
//    fun convertNameToId(artistId: String) {
//        viewModelScope.launch {
//            mainRepository.convertNameToId(artistId).collect {
//                _artistId.postValue(it)
//            }
//        }
//    }

    fun getLocation() {
        regionCode = runBlocking { dataStoreManager.location.first() }
        quality = runBlocking { dataStoreManager.quality.first() }
        language = runBlocking { dataStoreManager.getString(SELECTED_LANGUAGE).first() }
    }

    fun checkAllDownloadingSongs() {
        viewModelScope.launch {
            mainRepository.getDownloadingSongs().collect { songs ->
                songs?.forEach { song ->
                    mainRepository.updateDownloadState(
                        song.videoId,
                        DownloadState.STATE_NOT_DOWNLOADED,
                    )
                }
            }
            mainRepository.getPreparingSongs().collect { songs ->
                songs.forEach { song ->
                    mainRepository.updateDownloadState(
                        song.videoId,
                        DownloadState.STATE_NOT_DOWNLOADED,
                    )
                }
            }
        }
    }

    private fun getFormat(mediaId: String?) {
        if (mediaId != _format.value?.videoId && !mediaId.isNullOrEmpty()) {
            _format.value = null
            getFormatFlowJob?.cancel()
            getFormatFlowJob =
                viewModelScope.launch {
                    mainRepository.getFormatFlow(mediaId).cancellable().collectLatest { f ->
                        Log.w(tag, "Get format for $mediaId: $f")
                        if (f != null) {
                            _format.emit(f)
                        } else {
                            _format.emit(null)
                        }
                    }
                }
        }
    }

    private var songInfoJob: Job? = null

    fun getSongInfo(mediaId: String?) {
        songInfoJob?.cancel()
        songInfoJob =
            viewModelScope.launch {
                if (mediaId != null) {
                    mainRepository.getSongInfo(mediaId).collect { song ->
                        _nowPlayingScreenData.update {
                            it.copy(
                                songInfoData = song,
                            )
                        }
                    }
                }
            }
    }

    private var _githubResponse = MutableLiveData<GithubResponse?>()
    val githubResponse: LiveData<GithubResponse?> = _githubResponse

    fun checkForUpdate() {
        viewModelScope.launch {
            mainRepository.checkForUpdate().collect { response ->
                dataStoreManager.putString(
                    "CheckForUpdateAt",
                    System.currentTimeMillis().toString(),
                )
                _githubResponse.postValue(response)
            }
        }
    }

    fun stopPlayer() {
        _nowPlayingScreenData.value = NowPlayingScreenData.initial()
        _nowPlayingState.value = null
        simpleMediaServiceHandler.resetSongAndQueue()
        onUIEvent(UIEvent.Stop)
    }

    fun addToYouTubePlaylist(
        localPlaylistId: Long,
        youtubePlaylistId: String,
        videoId: String,
    ) {
        viewModelScope.launch {
            mainRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                localPlaylistId,
                LocalPlaylistEntity.YouTubeSyncState.Syncing,
            )
            mainRepository.addYouTubePlaylistItem(youtubePlaylistId, videoId).collect { response ->
                if (response == "STATUS_SUCCEEDED") {
                    mainRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                        localPlaylistId,
                        LocalPlaylistEntity.YouTubeSyncState.Synced,
                    )
                    Toast
                        .makeText(
                            getApplication(),
                            application.getString(R.string.added_to_youtube_playlist),
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    mainRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                        localPlaylistId,
                        LocalPlaylistEntity.YouTubeSyncState.NotSynced,
                    )
                    Toast
                        .makeText(
                            getApplication(),
                            application.getString(R.string.error),
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }
    }

    fun addQueueToPlayer() {
        simpleMediaServiceHandler.addQueueToPlayer()
    }

    private fun loadPlaylistOrAlbum(index: Int? = null) {
        simpleMediaServiceHandler.loadPlaylistOrAlbum(index)
    }

    private fun updateLyrics(
        videoId: String,
        lyrics: Lyrics?,
        isTranslatedLyrics: Boolean,
        lyricsProvider: LyricsProvider = LyricsProvider.MUSIXMATCH,
    ) {
        if (_nowPlayingState.value?.songEntity?.videoId == videoId) {
            when (isTranslatedLyrics) {
                true -> {
                    _nowPlayingScreenData.update {
                        it.copy(
                            lyricsData =
                                it.lyricsData?.copy(
                                    translatedLyrics = lyrics,
                                ),
                        )
                    }
                }
                false -> {
                    if (lyrics != null) {
                        _nowPlayingScreenData.update {
                            it.copy(
                                lyricsData =
                                    NowPlayingScreenData.LyricsData(
                                        lyrics = lyrics,
                                        lyricsProvider = lyricsProvider,
                                    ),
                            )
                        }
                        // Save lyrics to database
                        viewModelScope.launch {
                            mainRepository.insertLyrics(
                                LyricsEntity(
                                    videoId = videoId,
                                    error = false,
                                    lines = lyrics.lines,
                                    syncType = lyrics.syncType,
                                ),
                            )
                        }
                    } else {
                        _nowPlayingScreenData.update {
                            it.copy(
                                lyricsData = null,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getLyricsFromFormat(
        song: SongEntity,
        duration: Int,
    ) {
        viewModelScope.launch {
            val videoId = song.videoId
            Log.w(tag, "Get Lyrics From Format for $videoId")
            val artist =
                if (song.artistName?.firstOrNull() != null &&
                    song.artistName
                        .firstOrNull()
                        ?.contains("Various Artists") == false
                ) {
                    song.artistName.firstOrNull()
                } else {
                    simpleMediaServiceHandler.nowPlaying
                        .first()
                        ?.mediaMetadata
                        ?.artist
                        ?: ""
                }
            if (dataStoreManager.lyricsProvider.first() == DataStoreManager.MUSIXMATCH) {
                mainRepository
                    .getLyricsDataMacro(
                        (artist ?: "").toString(),
                        song.title,
                        duration,
                    ).cancellable()
                    .collectLatest { response ->
                        Log.w(tag, response.second.data.toString())

                        when (response.second) {
                            is Resource.Success -> {
                                if (response.second.data != null && response.second.data?.captchaRequired == false) {
                                    Log.d(tag, "Get Lyrics Data Success")
                                    updateLyrics(
                                        videoId,
                                        response.second.data,
                                        false,
                                        LyricsProvider.MUSIXMATCH,
                                    )
                                    insertLyrics(
                                        response.second.data!!.toLyricsEntity(
                                            videoId,
                                        ),
                                    )
                                    if (dataStoreManager.enableTranslateLyric.first() == TRUE) {
                                        val savedTranslatedLyrics = mainRepository.getSavedTranslatedLyrics(
                                            videoId,
                                            dataStoreManager.translationLanguage.first()
                                        ).firstOrNull()
                                        if (savedTranslatedLyrics != null) {
                                            Log.d(tag, "Get Saved Translated Lyrics")
                                            updateLyrics(
                                                videoId,
                                                savedTranslatedLyrics.toLyrics(),
                                                true,
                                            )
                                        } else {
                                            mainRepository
                                                .getTranslateLyrics(
                                                    response.first,
                                                ).cancellable()
                                                .collect { translate ->
                                                    if (translate != null) {
                                                        Log.d(tag, "Get Translate Lyrics Success")
                                                        updateLyrics(
                                                            videoId,
                                                            translate.toLyrics(
                                                                response.second.data!!,
                                                            ),
                                                            true,
                                                        )
                                                    }
                                                }
                                        }
                                    } else {
                                        getAITranslationLyrics(
                                            videoId,
                                            response.second.data ?: return@collectLatest,
                                        )
                                    }
                                } else if (response.second.data?.captchaRequired == true) {
                                    _showMusixmatchCaptchaWebView.value = true
                                } else if (dataStoreManager.spotifyLyrics.first() == TRUE) {
                                    getSpotifyLyrics(
                                        song.toTrack().copy(
                                            durationSeconds = duration,
                                        ),
                                        "${song.title} $artist",
                                        duration,
                                    )
                                }
                            }

                            is Resource.Error -> {
                                Log.w(tag, "Get Lyrics Data Error")
                                if (_lyrics.value?.message != "reset") {
                                    if (dataStoreManager.spotifyLyrics.first() == TRUE) {
                                        getSpotifyLyrics(
                                            song.toTrack().copy(
                                                durationSeconds = duration,
                                            ),
                                            "${song.title} $artist",
                                            duration,
                                        )
                                    } else {
                                        getLrclibLyrics(
                                            song,
                                            (artist ?: "").toString(),
                                            duration,
                                        )
                                    }
                                }
                            }
                        }
                    }
            } else if (dataStoreManager.lyricsProvider.first() == DataStoreManager.LRCLIB) {
                getLrclibLyrics(
                    song,
                    (artist ?: "").toString(),
                    duration,
                )
            } else if (dataStoreManager.lyricsProvider.first() == DataStoreManager.YOUTUBE) {
                mainRepository.getYouTubeCaption(videoId).cancellable().collect { response ->
                    _lyrics.value = response
                    when (response) {
                        is Resource.Success -> {
                            if (response.data != null) {
                                insertLyrics(response.data.toLyricsEntity(videoId))
                                updateLyrics(
                                    videoId,
                                    response.data,
                                    false,
                                    LyricsProvider.YOUTUBE,
                                )
                                getAITranslationLyrics(
                                    videoId,
                                    response.data,
                                )
                            } else if (dataStoreManager.spotifyLyrics.first() == TRUE) {
                                getSpotifyLyrics(
                                    song.toTrack().copy(
                                        durationSeconds = duration,
                                    ),
                                    "${song.title} ${song.artistName?.firstOrNull() ?: simpleMediaServiceHandler.nowPlaying
                                        .first()
                                        ?.mediaMetadata
                                        ?.artist ?: ""}",
                                    duration,
                                )
                            }
                        }

                        is Resource.Error -> {
                            if (_lyrics.value?.message != "reset") {
                                if (dataStoreManager.spotifyLyrics.first() == TRUE) {
                                    getSpotifyLyrics(
                                        song.toTrack().copy(
                                            durationSeconds = duration,
                                        ),
                                        "${song.title} ${song.artistName?.firstOrNull() ?: simpleMediaServiceHandler.nowPlaying
                                            .first()
                                            ?.mediaMetadata
                                            ?.artist ?: ""}",
                                        duration,
                                    )
                                } else {
                                    getLrclibLyrics(
                                        song,
                                        (artist ?: "").toString(),
                                        duration,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getLrclibLyrics(
        song: SongEntity,
        artist: String,
        duration: Int
    ) {
        viewModelScope.launch {
            mainRepository
                .getLrclibLyricsData(
                    artist,
                    song.title,
                    duration,
                ).collectLatest { res ->
                    when (res) {
                        is Resource.Success -> {
                            Log.d(tag, "Get Lyrics Data Success")
                            updateLyrics(
                                song.videoId,
                                res.data,
                                false,
                                LyricsProvider.LRCLIB,
                            )
                            insertLyrics(
                                res.data?.toLyricsEntity(
                                    song.videoId,
                                ) ?: return@collectLatest,
                            )
                            getAITranslationLyrics(
                                song.videoId,
                                res.data,
                            )
                        }
                        is Resource.Error -> {
                            getSavedLyrics(
                                song.toTrack().copy(
                                    durationSeconds = duration,
                                ),
                            )
                        }
                    }
                }
        }
    }

    private suspend fun getAITranslationLyrics(
        videoId: String,
        lyrics: Lyrics,
    ) {
        if (dataStoreManager.useAITranslation.first() == TRUE
            && dataStoreManager.aiApiKey.first().isNotEmpty()
            && dataStoreManager.enableTranslateLyric.first() == FALSE
        ) {
            val savedTranslatedLyrics = mainRepository.getSavedTranslatedLyrics(
                videoId,
                dataStoreManager.translationLanguage.first()
            ).firstOrNull()
            if (savedTranslatedLyrics != null) {
                Log.d(tag, "Get Saved Translated Lyrics")
                updateLyrics(
                    videoId,
                    savedTranslatedLyrics.toLyrics(),
                    true,
                )
            } else {
                mainRepository.getAITranslationLyrics(
                    lyrics,
                    dataStoreManager.translationLanguage.first()
                ).cancellable()
                    .collectLatest {
                        val data = it.data
                        when (it) {
                            is Resource.Success if (data != null) -> {
                                if (true) {
                                    Log.d(tag, "Get AI Translate Lyrics Success")
                                    mainRepository.insertTranslatedLyrics(
                                        TranslatedLyricsEntity(
                                            videoId = videoId,
                                            language = dataStoreManager.translationLanguage.first(),
                                            error = false,
                                            lines = data.lines,
                                            syncType = data.syncType,
                                        ),
                                    )
                                    updateLyrics(
                                        videoId,
                                        data,
                                        true,
                                    )
                                }
                            }

                            else -> {
                                Log.w(tag, "Get AI Translate Lyrics Error: ${it.message}")
                            }
                        }
                    }
            }
        }
    }

    private fun getSpotifyLyrics(
        track: Track,
        query: String,
        duration: Int? = null,
    ) {
        viewModelScope.launch {
            Log.d("Check SpotifyLyrics", "SpotifyLyrics $query")
            mainRepository.getSpotifyLyrics(query, duration).cancellable().collect { response ->
                Log.d("Check SpotifyLyrics", response.toString())
                _lyrics.value = response
                when (response) {
                    is Resource.Success -> {
                        if (response.data != null) {
                            insertLyrics(response.data.toLyricsEntity(query))
                            updateLyrics(
                                track.videoId,
                                response.data,
                                false,
                                LyricsProvider.SPOTIFY,
                            )
                            getAITranslationLyrics(
                                track.videoId,
                                response.data,
                            )
                        }
                    }

                    is Resource.Error -> {
                        if (_lyrics.value?.message != "reset") {
                            getLrclibLyrics(
                                track.toSongEntity(),
                                track.artists.toListName().firstOrNull() ?: "",
                                duration ?: 0,
                            )
                        }
                    }
                }
            }
        }
    }

    fun getLyricsProvider(): String = runBlocking { dataStoreManager.lyricsProvider.first() }

    fun setLyricsProvider() {
        viewModelScope.launch {
            nowPlayingState.value?.songEntity?.let {
                getLyricsFromFormat(it, timeline.value.total.toInt() / 1000)
            }
        }
    }

    fun updateInLibrary(videoId: String) {
        viewModelScope.launch {
            mainRepository.updateSongInLibrary(LocalDateTime.now(), videoId)
        }
    }

    fun insertPairSongLocalPlaylist(pairSongLocalPlaylist: PairSongLocalPlaylist) {
        viewModelScope.launch {
            mainRepository.insertPairSongLocalPlaylist(pairSongLocalPlaylist)
        }
    }

    private var _recreateActivity: MutableLiveData<Boolean> = MutableLiveData()
    val recreateActivity: LiveData<Boolean> = _recreateActivity

    fun activityRecreate() {
        _recreateActivity.value = true
    }

    fun activityRecreateDone() {
        _recreateActivity.value = false
    }

    fun addToQueue(track: Track) {
        viewModelScope.launch {
            simpleMediaServiceHandler.loadMoreCatalog(arrayListOf(track))
            Toast
                .makeText(
                    context,
                    context.getString(R.string.added_to_queue),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    fun addListLocalToQueue(listVideoId: List<String>) {
        viewModelScope.launch {
            val listSong = mainRepository.getSongsByListVideoId(listVideoId).singleOrNull()
            if (!listSong.isNullOrEmpty()) {
                simpleMediaServiceHandler.loadMoreCatalog(listSong.toArrayListTrack(), true)
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.added_to_queue),
                        Toast.LENGTH_SHORT,
                    ).show()
            } else {
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.error),
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    fun addListToQueue(listTrack: ArrayList<Track>) {
        viewModelScope.launch {
            simpleMediaServiceHandler.loadMoreCatalog(listTrack)
            Toast
                .makeText(
                    context,
                    context.getString(R.string.added_to_queue),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    fun playNext(song: Track) {
        viewModelScope.launch {
            simpleMediaServiceHandler.playNext(song)
            Toast
                .makeText(context, context.getString(R.string.play_next), Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun logInToYouTube() = dataStoreManager.loggedIn

    fun addToYouTubeLiked() {
        viewModelScope.launch {
            val videoId = simpleMediaServiceHandler.nowPlaying.first()?.mediaId
            if (videoId != null) {
                val like = likeStatus.value
                if (!like) {
                    mainRepository
                        .addToYouTubeLiked(
                            simpleMediaServiceHandler.nowPlaying.first()?.mediaId,
                        ).collect { response ->
                            if (response == 200) {
                                Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.added_to_youtube_liked),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                getLikeStatus(videoId)
                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.error),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                        }
                } else {
                    mainRepository
                        .removeFromYouTubeLiked(
                            simpleMediaServiceHandler.nowPlaying.first()?.mediaId,
                        ).collect {
                            if (it == 200) {
                                Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.removed_from_youtube_liked),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                getLikeStatus(videoId)
                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.error),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                        }
                }
            }
        }
    }

    fun getTranslucentBottomBar() = dataStoreManager.translucentBottomBar

    private var _homeRefresh: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val homeRefresh: StateFlow<Boolean> = _homeRefresh.asStateFlow()

    fun homeRefresh() {
        _homeRefresh.value = true
    }

    fun homeRefreshDone() {
        _homeRefresh.value = false
    }

    fun shouldCheckForUpdate(): Boolean = runBlocking { dataStoreManager.autoCheckForUpdates.first() == TRUE }

    fun runWorker() {
        Log.w("Check Worker", "Worker")
        val request =
            PeriodicWorkRequestBuilder<NotifyWork>(
                12L,
                TimeUnit.HOURS,
            ).addTag("Worker Test")
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                ).build()
        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
            "Artist Worker",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request,
        )
    }

    private var _downloadFileProgress = MutableStateFlow<DownloadProgress>(DownloadProgress.INIT)
    val downloadFileProgress: StateFlow<DownloadProgress> get() = _downloadFileProgress

    fun downloadFile() {
        val fileName =
            "${nowPlayingScreenData.value.nowPlayingTitle} - ${nowPlayingScreenData.value.artistName}"
                .replace(Regex("""[|\\?*<":>]"""), "")
                .replace(" ", "_")
        val path =
            "${Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS,
            ).path}/$fileName"
        viewModelScope.launch {
            nowPlayingState.value?.songEntity?.videoId?.let { videoId ->
                mainRepository
                    .downloadToFile(
                        videoId = videoId,
                        path = path,
                        isVideo = nowPlayingScreenData.value.isVideo,
                    ).collectLatest {
                        _downloadFileProgress.value = it
                    }
            }
        }
    }

    fun downloadFileDone() {
        _downloadFileProgress.value = DownloadProgress.INIT
    }

    fun onDoneReview(isDismissOnly: Boolean = true) {
        viewModelScope.launch {
            if (!isDismissOnly) {
                dataStoreManager.doneOpenAppTime()
            } else {
                dataStoreManager.openApp()
            }
        }
    }
}

sealed class UIEvent {
    data object PlayPause : UIEvent()

    data object Backward : UIEvent()

    data object Forward : UIEvent()

    data object Next : UIEvent()

    data object Previous : UIEvent()

    data object Stop : UIEvent()

    data object Shuffle : UIEvent()

    data object Repeat : UIEvent()

    data class UpdateProgress(
        val newProgress: Float,
    ) : UIEvent()

    data object ToggleLike : UIEvent()
}

enum class LyricsProvider {
    MUSIXMATCH,
    YOUTUBE,
    SPOTIFY,
    LRCLIB,
    OFFLINE,
}

data class TimeLine(
    val current: Long,
    val total: Long,
    val bufferedPercent: Int,
    val loading: Boolean = true,
)

data class NowPlayingScreenData(
    val playlistName: String,
    val nowPlayingTitle: String,
    val artistName: String,
    val isVideo: Boolean,
    val isExplicit: Boolean = false,
    val thumbnailURL: String?,
    val canvasData: CanvasData? = null,
    val lyricsData: LyricsData? = null,
    val songInfoData: SongInfoEntity? = null,
) {
    data class CanvasData(
        val isVideo: Boolean,
        val url: String,
    )

    data class LyricsData(
        val lyrics: Lyrics,
        val translatedLyrics: Lyrics? = null,
        val lyricsProvider: LyricsProvider,
    )

    companion object {
        fun initial(): NowPlayingScreenData =
            NowPlayingScreenData(
                nowPlayingTitle = "",
                artistName = "",
                isVideo = false,
                thumbnailURL = null,
                canvasData = null,
                lyricsData = null,
                songInfoData = null,
                playlistName = "",
            )
    }
}