package com.abcg.music.ui.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abcg.music.data.backup.BackupRepository
import com.abcg.music.data.backup.RestoreResult
import com.abcg.music.data.generate.GenerateRepository
import com.abcg.music.data.local.AccentMode
import com.abcg.music.data.local.AppLanguage
import com.abcg.music.data.local.EqualizerSettings
import com.abcg.music.data.local.LyricsUiVersion
import com.abcg.music.data.local.MiscSettings
import com.abcg.music.data.local.ScrobblerPreferences
import com.abcg.music.data.local.ScrobblerSettings
import com.abcg.music.data.local.SessionData
import com.abcg.music.data.local.SessionPreferences
import com.abcg.music.data.local.SettingsPreferences
import com.abcg.music.data.playlist.PlaylistRepository
import com.abcg.music.data.repository.AuthRepository
import com.abcg.music.data.repository.ThemeRepository
import com.abcg.music.data.repository.ThemeUiState
import com.abcg.music.playback.NativeAudioEngine
import com.abcg.music.util.FileExportHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PendingRestoreKind { FULL_BACKUP, PLAYLIST_MIRROR }

private val SettingsSharing = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000)
private const val SETTINGS_TAG = "SettingsViewModel"

/** Settings aggregates several independent stores. A broken legacy value or
 * one unreadable Room table must only disable that section, never crash the
 * entire destination. Flow.catch preserves cancellation and handles only
 * failures from the upstream source. */
private fun <T> Flow<T>.withSettingsFallback(source: String, fallback: T): Flow<T> =
    catch { error ->
        android.util.Log.e(SETTINGS_TAG, "$source unavailable; using safe defaults", error)
        emit(fallback)
    }

data class SettingsScreenState(
    val session: SessionData = SessionData(),
    val theme: ThemeUiState? = null,
    val misc: MiscSettings = MiscSettings(),
    val recommendationExclusionCount: Int = 0,
    val toastMessage: String? = null,
    val showColorWheel: Boolean = false,
    val showClearAllConfirm: Boolean = false,
    val showRestoreConfirm: Boolean = false,
    val pendingRestoreContent: String? = null,
    val pendingRestorePlaylistCount: Int? = null,
    val pendingRestoreKind: PendingRestoreKind? = null,
    val pendingRestoreUri: android.net.Uri? = null,
    val showSessionKeyDialog: Boolean = false,
    val sessionKeyError: String? = null,
    val sessionKeyLoading: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionPreferences: SessionPreferences,
    private val themeRepository: ThemeRepository,
    private val settingsPreferences: SettingsPreferences,
    private val audioEngine: dagger.Lazy<NativeAudioEngine>,
    private val generateRepository: GenerateRepository,
    private val discoverRepository: com.abcg.music.data.discover.DiscoverRepository,
    private val backupRepository: BackupRepository,
    private val playlistRepository: PlaylistRepository,
    private val fileExportHelper: FileExportHelper,
    private val scrobblerPreferences: ScrobblerPreferences,
    private val equalizerPreferences: com.abcg.music.data.local.EqualizerPreferences,
    private val ytAuthManager: com.abcg.music.data.ytmusic.YtMusicAuthManager,
    private val ytMusicSyncManager: com.abcg.music.data.ytmusic.YtMusicSyncManager,
    private val ytMusicPreferences: com.abcg.music.data.ytmusic.YtMusicPreferences,
    private val ytMusicLibraryManager: com.abcg.music.data.ytmusic.YtMusicLibraryManager,
    private val downloadedTrackDao: com.abcg.music.data.local.db.DownloadedTrackDao,
    private val appLocaleManager: com.abcg.music.util.AppLocaleManager,
    val playlistImportManager: com.abcg.music.data.playlist.PlaylistImportManager,
    val innerTube: com.abcg.music.data.music.InnerTubeMusicApi,
    val appUpdateManager: com.abcg.music.data.update.AppUpdateManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
) : ViewModel() {

    val authState: StateFlow<com.abcg.music.data.model.AuthState> = authRepository.authState
    val updateInfo = appUpdateManager.updateInfo

    fun checkForUpdates() = appUpdateManager.checkForUpdate(isSilent = false)
    fun openUpdate(context: android.content.Context) = appUpdateManager.openUpdate(context)

    /** YouTube Music account connection + playlist-sync state (§ YouTube Music). */
    val ytConnection: StateFlow<com.abcg.music.data.ytmusic.YtConnection> = ytAuthManager.connection
    val ytSyncState: StateFlow<com.abcg.music.data.ytmusic.YtSyncState> = ytMusicSyncManager.state
    val ytSyncEnabled: StateFlow<Boolean> = ytMusicPreferences.syncEnabled
        .withSettingsFallback("YouTube sync preference", false)
        .stateIn(viewModelScope, SettingsSharing, false)
    /** History sync defaults ON (see YtMusicPreferences.historySyncEnabled). */
    val ytHistorySyncEnabled: StateFlow<Boolean> = ytMusicPreferences.historySyncEnabled
        .withSettingsFallback("YouTube history sync preference", true)
        .stateIn(viewModelScope, SettingsSharing, true)
    val ytLastSyncAt: StateFlow<Long> = ytMusicPreferences.lastSyncAt
        .withSettingsFallback("YouTube sync timestamp", 0L)
        .stateIn(viewModelScope, SettingsSharing, 0L)
    val syncedPlaylistIds: StateFlow<Set<Long>?> = ytMusicPreferences.syncedPlaylistIds
        .withSettingsFallback("YouTube playlist selection", null)
        .stateIn(viewModelScope, SettingsSharing, null)
    val ytAccountPlaylists = ytMusicLibraryManager.accountPlaylists
    val hiddenYtLibraryPlaylistIds: StateFlow<Set<String>> = ytMusicPreferences.hiddenLibraryPlaylistIds
        .withSettingsFallback("YouTube library visibility", emptySet())
        .stateIn(viewModelScope, SettingsSharing, emptySet())
    val allPlaylists: StateFlow<List<com.abcg.music.data.playlist.SavedPlaylist>> = playlistRepository.playlists
        .map { playlists -> playlists.filterNot { it.mode == com.abcg.music.data.playlist.LIKED_SONGS_MODE } }
        .withSettingsFallback("playlists", emptyList())
        .stateIn(viewModelScope, SettingsSharing, emptyList())

    val session: StateFlow<SessionData> = kotlinx.coroutines.flow.combine(
        sessionPreferences.session,
        authRepository.authState,
    ) { sess, auth ->
        if (sess.username.isNotBlank()) {
            sess
        } else if (auth is com.abcg.music.data.model.AuthState.SignedIn && auth.username.isNotBlank()) {
            sess.copy(username = auth.username)
        } else {
            sess
        }
    }
        .withSettingsFallback("session", SessionData())
        .stateIn(viewModelScope, SettingsSharing, SessionData())

    val theme: StateFlow<ThemeUiState> = themeRepository.uiState

    val misc: StateFlow<MiscSettings> = settingsPreferences.settings
        .withSettingsFallback("misc preferences", MiscSettings())
        .stateIn(viewModelScope, SettingsSharing, MiscSettings())

    val scrobbler: StateFlow<ScrobblerSettings> = scrobblerPreferences.settings
        .withSettingsFallback("scrobbler preferences", ScrobblerSettings())
        .stateIn(viewModelScope, SettingsSharing, ScrobblerSettings())

    /** Experimental 15-band equalizer state (Settings → Experimental). */
    val equalizer: StateFlow<EqualizerSettings> = equalizerPreferences.settings
        .withSettingsFallback("equalizer preferences", EqualizerSettings())
        .stateIn(viewModelScope, SettingsSharing, EqualizerSettings())
    private var immediateEqGains = EqualizerSettings().gainsDb.toFloatArray()
    private var immediateEqEnabled = false

    val downloadCount: StateFlow<Int> = downloadedTrackDao.count()
        .withSettingsFallback("download count", 0)
        .stateIn(viewModelScope, SettingsSharing, 0)

    val downloadTotalBytes: StateFlow<Long?> = downloadedTrackDao.totalBytes()
        .withSettingsFallback("download size", 0L)
        .stateIn(viewModelScope, SettingsSharing, 0L)

    private val _uiState = MutableStateFlow(SettingsScreenState())
    val uiState: StateFlow<SettingsScreenState> = _uiState.asStateFlow()

    /** Prevent DataStore/Room/runtime write failures from escaping as an
     * uncaught root coroutine and terminating the app. */
    private fun launchSettingsAction(action: String, block: suspend () -> Unit) =
        viewModelScope.launch {
            try {
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                android.util.Log.e(SETTINGS_TAG, "Failed to $action", error)
                _uiState.update { state ->
                    state.copy(toastMessage = "Couldn't $action. Please try again.")
                }
            } catch (error: LinkageError) {
                // Missing/altered framework or JNI symbols on a custom ROM
                // disable only this action, never the Settings destination.
                android.util.Log.e(SETTINGS_TAG, "Unsupported platform action: $action", error)
                _uiState.update { state ->
                    state.copy(toastMessage = "This action isn't supported on this device.")
                }
            }
        }

    private val eqPreviews = Channel<Pair<Boolean, FloatArray>>(Channel.CONFLATED)

    private suspend fun applyNativeAudio(block: (NativeAudioEngine) -> Unit) =
        withContext(Dispatchers.Default) { block(audioEngine.get()) }

    init {
        viewModelScope.launch {
            for ((enabled, gains) in eqPreviews) {
                try {
                    applyNativeAudio { it.setEqualizer(enabled, gains) }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    android.util.Log.e(SETTINGS_TAG, "Equalizer preview failed", error)
                    _uiState.update { it.copy(toastMessage = "Couldn't apply equalizer settings.") }
                } catch (error: LinkageError) {
                    android.util.Log.e(SETTINGS_TAG, "Equalizer unavailable", error)
                    _uiState.update { it.copy(toastMessage = "Equalizer isn't available on this device.") }
                }
            }
        }
        viewModelScope.launch {
            equalizer.collect {
                immediateEqEnabled = it.enabled
                immediateEqGains = it.gainsDb.toFloatArray()
            }
        }
        viewModelScope.launch {
            generateRepository.observeRecommendationExclusions()
                .withSettingsFallback("recommendation exclusions", emptyList())
                .collect { exclusions ->
                    _uiState.update { it.copy(recommendationExclusionCount = exclusions.size) }
                }
        }
    }

    fun refreshRecommendationExclusionCount() {
        launchSettingsAction("refresh exclusions") {
            val count = generateRepository.recommendationExclusionCount()
            _uiState.update { it.copy(recommendationExclusionCount = count) }
        }
    }

    fun saveApiCredentials(apiKey: String, apiSecret: String) {
        launchSettingsAction("save API credentials") { authRepository.saveApiCredentials(apiKey, apiSecret) }
    }

    fun logOut(onComplete: () -> Unit) {
        launchSettingsAction("log out") {
            sessionPreferences.logOutApiCredentials()
            onComplete()
        }
    }

    fun clearSession(onComplete: () -> Unit) {
        launchSettingsAction("clear the session") {
            sessionPreferences.clearAll()
            onComplete()
        }
    }

    // ── Appearance (§8.2 / §8.3 / §8.4) ──

    fun setAmoled(enabled: Boolean) = launchSettingsAction("update AMOLED mode") { themeRepository.setAmoled(enabled) }
    fun setLiquidGlass(enabled: Boolean) = launchSettingsAction("update Liquid Glass") { themeRepository.setLiquidGlass(enabled) }
    fun setAccentMode(mode: AccentMode) = launchSettingsAction("update accent mode") { themeRepository.setMode(mode) }
    fun setManualAccent(color: Color) = launchSettingsAction("update accent color") { themeRepository.setManualAccent(color) }
    fun openColorWheel() = _uiState.update { it.copy(showColorWheel = true) }
    fun dismissColorWheel() = _uiState.update { it.copy(showColorWheel = false) }
    fun applyCustomColor(color: Color) {
        setManualAccent(color)
        dismissColorWheel()
    }

    fun setDynamicNowPlaying(enabled: Boolean) = launchSettingsAction("update dynamic theme") { themeRepository.setDynamicNowPlaying(enabled) }
    fun setUseCustomFont(enabled: Boolean) = launchSettingsAction("update the app font") { settingsPreferences.setUseCustomFont(enabled) }
    fun setPreferLosslessStreaming(enabled: Boolean) = launchSettingsAction("update streaming preference") { settingsPreferences.setPreferLosslessStreaming(enabled) }
    fun setLosslessQuality(quality: Int) = launchSettingsAction("update streaming quality") { settingsPreferences.setLosslessQuality(quality) }
    fun setDownloadQuality(quality: Int) = launchSettingsAction("update download quality") { settingsPreferences.setDownloadQuality(quality) }
    fun setStudioMasterClarity(enabled: Boolean) {
        // Apply immediately; DataStore persists the same state for future engine instances.
        launchSettingsAction("update Studio Master Clarity") {
            applyNativeAudio { it.setStudioMasterClarity(enabled) }
            settingsPreferences.setStudioMasterClarity(enabled)
            if (enabled) {
                // Enabling DSP clarity disables Bit-Perfect mode
                settingsPreferences.setBitPerfectEnabled(false)
                applyNativeAudio { it.setBitPerfect(false) }
            }
        }
    }
    fun setBitPerfectEnabled(enabled: Boolean) {
        launchSettingsAction("update Bit-Perfect mode") {
            applyNativeAudio { it.setBitPerfect(enabled) }
            settingsPreferences.setBitPerfectEnabled(enabled)
            if (enabled) {
                // When Bit-Perfect is turned on, automatically turn off Studio Master Clarity
                settingsPreferences.setStudioMasterClarity(false)
                applyNativeAudio { it.setStudioMasterClarity(false) }
            }
        }
    }
    fun setLyricsUiVersion(version: LyricsUiVersion) = launchSettingsAction("update lyrics UI version") { settingsPreferences.setLyricsUiVersion(version) }
    fun setWordByWordLyrics(enabled: Boolean) = launchSettingsAction("update word-by-word lyrics") { settingsPreferences.setWordByWordLyrics(enabled) }
    fun setLyricsAnimation(animation: com.abcg.music.data.local.LyricsAnimation) = launchSettingsAction("update lyrics animation") { settingsPreferences.setLyricsAnimation(animation) }
    fun setCrossfadeEnabled(enabled: Boolean) = launchSettingsAction("update crossfade") { settingsPreferences.setCrossfadeEnabled(enabled) }
    fun setCrossfadeSeconds(seconds: Int) = launchSettingsAction("update crossfade duration") {
        settingsPreferences.setCrossfadeSeconds(seconds.coerceIn(1, 12))
    }
    fun setWavySeekbarEnabled(enabled: Boolean) = launchSettingsAction("update seekbar style") {
        settingsPreferences.setWavySeekbarEnabled(enabled)
    }
    fun setDownloadLyrics(enabled: Boolean) = launchSettingsAction("update download lyrics setting") {
        settingsPreferences.setDownloadLyrics(enabled)
    }

    /** Persists + applies; AppLocaleManager owns error handling and threading. */
    fun setAppLanguage(language: AppLanguage) {
        appLocaleManager.applyLanguage(language)
    }

    // ── Experimental: 15-band equalizer ──

    fun setEqualizerEnabled(enabled: Boolean) {
        immediateEqEnabled = enabled
        eqPreviews.trySend(enabled to immediateEqGains.copyOf())
        launchSettingsAction("update the equalizer") { equalizerPreferences.setEnabled(enabled) }
    }

    /** Selecting a preset also switches the EQ on — an off equalizer with a
     *  fresh preset would read as a dead control. */
    fun applyEqPreset(name: String) {
        com.abcg.music.data.local.EqualizerPresets.byName(name)?.let { preset ->
            immediateEqEnabled = true
            immediateEqGains = preset.gainsDb.toFloatArray()
            eqPreviews.trySend(true to immediateEqGains.copyOf())
            launchSettingsAction("apply the equalizer preset") { equalizerPreferences.applyPreset(preset) }
        }
    }

    /** Audible preview during drag; persistence is deferred until release. */
    fun previewEqBandGain(bandIndex: Int, gainDb: Float) {
        if (bandIndex !in immediateEqGains.indices || !gainDb.isFinite()) return
        immediateEqGains = immediateEqGains.copyOf().also {
            it[bandIndex] = gainDb.coerceIn(
                -com.abcg.music.data.local.EQ_MAX_GAIN_DB,
                com.abcg.music.data.local.EQ_MAX_GAIN_DB,
            )
        }
        eqPreviews.trySend(immediateEqEnabled to immediateEqGains.copyOf())
    }

    /** Manual band drag → curve becomes Custom. */
    fun setEqBandGain(bandIndex: Int, gainDb: Float) {
        if (bandIndex !in immediateEqGains.indices) return
        previewEqBandGain(bandIndex, gainDb)
        launchSettingsAction("update the equalizer band") { equalizerPreferences.setBandGain(bandIndex, gainDb) }
    }

    // ── Data management (§8.5) ──

    fun clearRecommendationExclusions() {
        launchSettingsAction("clear exclusions") {
            discoverRepository.clearRecommendationExclusions()
            refreshRecommendationExclusionCount()
            _uiState.update { it.copy(toastMessage = "Exclusion history cleared") }
        }
    }

    fun requestClearAllData() = _uiState.update { it.copy(showClearAllConfirm = true) }
    fun dismissClearAllConfirm() = _uiState.update { it.copy(showClearAllConfirm = false) }
    fun confirmClearAllData(onComplete: () -> Unit) {
        launchSettingsAction("clear saved data") {
            sessionPreferences.clearAll()
            discoverRepository.clearRecommendationExclusions()
            playlistRepository.clearAll()
            _uiState.update { it.copy(showClearAllConfirm = false) }
            onComplete()
        }
    }

    // ── Backup & Restore (§8.6) ──

    fun exportBackup(uri: android.net.Uri, appVersionName: String) {
        viewModelScope.launch {
            try {
                val json = backupRepository.buildBackup(appVersionName)
                if (json.isBlank()) {
                    _uiState.update { it.copy(toastMessage = "Backup creation failed: empty data") }
                    return@launch
                }
                val bytesWritten = fileExportHelper.writeTextToUri(uri, json)
                _uiState.update { it.copy(toastMessage = "Backup saved successfully (${bytesWritten / 1024} KB)") }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Backup failed: ${e.localizedMessage ?: e.message}") }
            }
        }
    }

    fun handleRestorePicked(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val content = fileExportHelper.readTextFromUri(uri)
                if (content.isNullOrBlank()) {
                    _uiState.update { it.copy(toastMessage = "Selected file is empty or unreadable") }
                    return@launch
                }
                stagePendingRestore(content, uri)
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Restore read error: ${e.localizedMessage ?: e.message}") }
            }
        }
    }

    fun handleCsvPicked(uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Extract filename
                val cursor = context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                val displayName = cursor?.use {
                    if (it.moveToFirst()) it.getString(0) else null
                } ?: "Imported Playlist"

                val fileType = displayName.substringAfterLast('.', "File").uppercase()
                _uiState.update { it.copy(toastMessage = "Matching and importing $fileType songs...") }

                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: error("Could not open selected playlist file")

                val (saved, result) = inputStream.use { playlistImportManager.importCsvStream(it, displayName) }
                _uiState.update {
                    it.copy(toastMessage = "Imported \"${saved.title}\": ${result.matchedCount} tracks, ${result.totalRows - result.matchedCount} skipped")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(toastMessage = "Import failed: ${e.localizedMessage ?: e.message}")
                }
            }
        }
    }

    /** Called once the file picker returns raw file content — validates and
     *  stages the restore, showing a confirm dialog with the item count
     *  before actually applying anything (§8.6). */
    fun stagePendingRestore(content: String, uri: android.net.Uri) {
        launchSettingsAction("stage the restore") {
            val backup = try {
                kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    .decodeFromString(com.abcg.music.data.backup.BackupFile.serializer(), content)
                    .takeIf { it.type == "lastwave-backup" }
            } catch (e: Exception) { null }
            val mirrorCount = playlistRepository.publicMirrorPlaylistCount(content)
            if (backup == null && mirrorCount == null) {
                _uiState.update { it.copy(toastMessage = "That isn't a LastWave backup or playlist JSON") }
                return@launchSettingsAction
            }
            _uiState.update {
                it.copy(
                    showRestoreConfirm = true,
                    pendingRestoreContent = content,
                    pendingRestorePlaylistCount = backup?.playlists?.size ?: mirrorCount,
                    pendingRestoreKind = if (backup != null) PendingRestoreKind.FULL_BACKUP else PendingRestoreKind.PLAYLIST_MIRROR,
                    pendingRestoreUri = uri,
                )
            }
        }
    }

    fun dismissRestoreConfirm() = _uiState.update {
        it.copy(
            showRestoreConfirm = false,
            pendingRestoreContent = null,
            pendingRestorePlaylistCount = null,
            pendingRestoreKind = null,
            pendingRestoreUri = null,
        )
    }

    fun confirmRestore(onComplete: () -> Unit) {
        val pending = _uiState.value
        val content = pending.pendingRestoreContent ?: return
        launchSettingsAction("restore the backup") {
            if (pending.pendingRestoreKind == PendingRestoreKind.PLAYLIST_MIRROR) {
                pending.pendingRestoreUri?.let(fileExportHelper::rememberPlaylistMirrorUri)
                playlistRepository.importPublicMirror(content)
                    .onSuccess { count ->
                        _uiState.update {
                            it.copy(
                                showRestoreConfirm = false,
                                pendingRestoreContent = null,
                                pendingRestoreKind = null,
                                pendingRestoreUri = null,
                                toastMessage = "Synced $count playlist(s) from local JSON",
                            )
                        }
                        kotlinx.coroutines.delay(900)
                        onComplete()
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(showRestoreConfirm = false, toastMessage = "Playlist sync failed: ${error.message}")
                        }
                    }
                return@launchSettingsAction
            }

            when (val result = backupRepository.restore(content)) {
                is RestoreResult.Success -> {
                    generateRepository.invalidateRecommendationExclusionCache()
                    discoverRepository.reset()
                    refreshRecommendationExclusionCount()
                    val exclusionNote = if (result.exclusionCount > 0) " and recommendation exclusions" else ""
                    _uiState.update {
                        it.copy(
                            showRestoreConfirm = false,
                            pendingRestoreContent = null,
                            toastMessage = "Restored ${result.playlistCount} playlist(s)$exclusionNote",
                        )
                    }
                    kotlinx.coroutines.delay(900)
                    onComplete()
                }
                RestoreResult.UnsupportedSchema -> _uiState.update { it.copy(showRestoreConfirm = false, toastMessage = "This backup was made with a newer version of LastWave") }
                RestoreResult.InvalidFile -> _uiState.update { it.copy(showRestoreConfirm = false, toastMessage = "That file doesn't look like a LastWave backup") }
                is RestoreResult.Failed -> _uiState.update { it.copy(showRestoreConfirm = false, toastMessage = "Restore failed: ${result.message}") }
            }
        }
    }

    fun showToast(message: String) = _uiState.update { it.copy(toastMessage = message) }

    fun dismissToast() = _uiState.update { it.copy(toastMessage = null) }

    // ── Diagnostics ──

    /** Builds a troubleshooting report (app/device info, notification-listener
     *  grant, widget snapshot + placed-widget count, and this process's own
     *  logcat — readable without any permission) and opens the system share
     *  sheet for it via the existing FileProvider export path. Runs off the
     *  main thread; failures surface as a toast through [launchSettingsAction]. */
    fun exportDiagnostics() {
        launchSettingsAction("export diagnostics") {
            val report = withContext(Dispatchers.IO) { buildDiagnosticsReport() }
            val filename = "lastwave-diagnostics-${System.currentTimeMillis()}.txt"
            fileExportHelper.shareFile(filename, report, "text/plain")
            _uiState.update { it.copy(toastMessage = "Diagnostics ready to share") }
        }
    }

    private suspend fun buildDiagnosticsReport(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.appendLine("LastWave diagnostics")
        sb.appendLine("time=${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}")
        val versionName = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "unknown"
        val versionCode = runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION") context.packageManager.getPackageInfo(context.packageName, 0).versionCode.toString()
            }
        }.getOrNull() ?: "unknown"
        sb.appendLine("app=${context.packageName} version=$versionName ($versionCode)")
        sb.appendLine("device=${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} sdk=${android.os.Build.VERSION.SDK_INT}")
        val hasNotificationAccess = runCatching {
            androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        }.getOrDefault(false)
        sb.appendLine("notificationListenerAccess=$hasNotificationAccess")
        val snapshot = runCatching { com.abcg.music.widget.NowPlayingWidgetSnapshot.read(context) }.getOrNull()
        if (snapshot == null) {
            sb.appendLine("widgetSnapshot=<unreadable>")
        } else {
            val artExists = snapshot.artPath?.let { java.io.File(it).exists() } ?: false
            sb.appendLine("widgetSnapshot: hasSession=${snapshot.hasSession} isPlaying=${snapshot.isPlaying}")
            sb.appendLine("  title=${snapshot.title} artist=${snapshot.artist} album=${snapshot.album}")
            sb.appendLine("  sourceApp=${snapshot.sourceApp} sourcePackage=${snapshot.sourcePackage}")
            sb.appendLine("  artPath=${snapshot.artPath} artExists=$artExists")
        }
        val placedWidgets = runCatching {
            androidx.glance.appwidget.GlanceAppWidgetManager(context)
                .getGlanceIds(com.abcg.music.widget.NowPlayingWidget::class.java).size
        }.getOrNull()
        sb.appendLine("placedGlanceWidgets=${placedWidgets ?: "<lookup failed>"}")
        sb.appendLine("---- logcat (this process) ----")
        sb.append(readOwnLogcat())
        sb.appendLine("---- end ----")
        sb.toString()
    }

    /** Dumps this process's logcat ring buffer. An app may always read its
     *  own logs without any permission; capped to the newest lines so the
     *  share sheet stays responsive. Never throws — failures become a
     *  one-line note. */
    private fun readOwnLogcat(): String = runCatching {
        val pid = android.os.Process.myPid().toString()
        val process = ProcessBuilder("logcat", "-d", "-v", "threadtime", "--pid", pid)
            .redirectErrorStream(true)
            .start()
        val lines = ArrayDeque<String>()
        process.inputStream.bufferedReader(Charsets.UTF_8).useLines { seq ->
            seq.forEach { line ->
                lines.addLast(line)
                if (lines.size > LOGCAT_MAX_LINES) lines.removeFirst()
            }
        }
        if (!process.waitFor(LOGCAT_TIMEOUT_SEC, java.util.concurrent.TimeUnit.SECONDS)) {
            runCatching { process.destroy() }
        }
        lines.joinToString("\n").ifBlank { "(empty log buffer)" }
    }.getOrElse { "(logcat unavailable: ${it.message})" }

    private companion object {
        const val LOGCAT_MAX_LINES = 3000
        const val LOGCAT_TIMEOUT_SEC = 8L
    }

    // ── Scrobbler ──

    /** The master toggle only turns scrobbling on if a session key already
     *  exists — track.scrobble/updateNowPlaying are signed calls this app
     *  can't make without one. If it's missing, this opens the password
     *  dialog instead of silently flipping a switch that wouldn't actually
     *  do anything yet; the toggle itself gets set once that succeeds. */
    fun setScrobblerEnabled(enabled: Boolean) {
        if (enabled && session.value.sessionKey.isBlank()) {
            _uiState.update { it.copy(showSessionKeyDialog = true) }
            return
        }
        launchSettingsAction("update scrobbling") { scrobblerPreferences.setEnabled(enabled) }
    }

    fun setSubmitNowPlaying(enabled: Boolean) = launchSettingsAction("update Now Playing submission") { scrobblerPreferences.setSubmitNowPlaying(enabled) }
    fun setPeriodicPollingEnabled(enabled: Boolean) = launchSettingsAction("update session polling") { scrobblerPreferences.setPeriodicPollingEnabled(enabled) }
    fun setScrobblePercent(percent: Int) = launchSettingsAction("update the scrobble threshold") { scrobblerPreferences.setScrobblePercent(percent) }

    // ── YouTube Music account ──

    /** Sync only turns on with a connected account; flipping it on triggers
     *  an immediate first mirror pass instead of waiting for the next tick. */
    fun setYtSyncEnabled(enabled: Boolean) {
        if (enabled && !ytConnection.value.isConnected) return
        launchSettingsAction("update YouTube sync") {
            ytMusicPreferences.setSyncEnabled(enabled)
            if (enabled) runCatching { ytMusicSyncManager.syncNow("enabled") }
        }
    }

    /** History sync only turns on with a connected account. Turning it off
     *  stops new submissions and cancels pending sync work (handled by
     *  YtMusicHistorySyncManager); the explicit choice itself is preserved
     *  across disconnects by YtMusicPreferences. */
    fun setYtHistorySyncEnabled(enabled: Boolean) {
        if (enabled && !ytConnection.value.isConnected) return
        launchSettingsAction("update YouTube history sync") {
            ytMusicPreferences.setHistorySyncEnabled(enabled)
        }
    }

    fun disconnectYouTube() {
        launchSettingsAction("disconnect YouTube Music") {
            ytMusicPreferences.setSyncEnabled(false)
            ytAuthManager.signOut()
            _uiState.update { it.copy(toastMessage = "YouTube Music disconnected") }
        }
    }

    fun togglePlaylistSync(playlistId: Long, enabled: Boolean) {
        launchSettingsAction("update playlist sync") {
            val allIds = allPlaylists.value.map { it.id }
            ytMusicPreferences.togglePlaylistSync(allIds, playlistId, enabled)
            if (ytSyncEnabled.value) {
                runCatching { ytMusicSyncManager.syncNow("selection_change") }
            }
        }
    }

    fun selectAllPlaylistsForSync(select: Boolean) {
        launchSettingsAction("update playlist sync") {
            val allIds = if (select) allPlaylists.value.map { it.id }.toSet() else emptySet()
            ytMusicPreferences.setSyncedPlaylistIds(allIds)
            if (ytSyncEnabled.value) {
                runCatching { ytMusicSyncManager.syncNow("selection_change") }
            }
        }
    }

    fun setYtLibraryPlaylistVisible(playlistId: String, visible: Boolean) {
        launchSettingsAction("update YouTube playlist visibility") {
            ytMusicPreferences.setLibraryPlaylistVisible(playlistId, visible)
        }
    }

    fun setAllYtLibraryPlaylistsVisible(visible: Boolean) {
        launchSettingsAction("update YouTube playlist visibility") {
            ytMusicPreferences.setAllLibraryPlaylistsVisible(
                playlistIds = ytAccountPlaylists.value.mapTo(mutableSetOf()) { it.id },
                visible = visible,
            )
        }
    }

    fun syncYouTubeNow() {
        launchSettingsAction("sync YouTube Music") { ytMusicSyncManager.syncNow("manual") }
    }

    fun dismissSessionKeyDialog() = _uiState.update { it.copy(showSessionKeyDialog = false, sessionKeyError = null) }

    fun submitPassword(password: String) {
        _uiState.update { it.copy(sessionKeyLoading = true, sessionKeyError = null) }
        launchSettingsAction("enable scrobbling") {
            when (val result = authRepository.obtainSessionKey(password)) {
                AuthRepository.SessionKeyResult.Success -> {
                    scrobblerPreferences.setEnabled(true)
                    _uiState.update { it.copy(showSessionKeyDialog = false, sessionKeyLoading = false, toastMessage = "Scrobbling enabled") }
                }
                is AuthRepository.SessionKeyResult.Failed -> {
                    _uiState.update { it.copy(sessionKeyLoading = false, sessionKeyError = result.message) }
                }
            }
        }
    }
}
