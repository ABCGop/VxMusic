package com.abcg.music.data.playlist

import com.abcg.music.data.generate.GeneratedTrack
import com.abcg.music.data.music.InnerTubeMusicApi
import com.abcg.music.data.music.YouTubePlaylistResult
import com.abcg.music.data.ytmusic.YtMusicPreferences
import com.abcg.music.data.ytmusic.YtPlaylistMapping
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistImportManager @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val innerTube: InnerTubeMusicApi,
    private val csvPlaylistImporter: CsvPlaylistImporter,
    private val ytMusicPreferences: YtMusicPreferences,
) {

    suspend fun importYouTubePlaylist(
        playlist: YouTubePlaylistResult,
        selectedTracks: List<com.abcg.music.data.music.YouTubeMusicTrack> = playlist.tracks,
    ): SavedPlaylist = withContext(Dispatchers.IO) {
        val tracks = selectedTracks.map { yt ->
            GeneratedTrack(
                name = yt.title,
                artist = yt.artist,
                album = yt.album,
                artworkUrl = yt.artworkUrl,
                url = "https://music.youtube.com/watch?v=${yt.videoId}",
            )
        }

        playlistRepository.save(
            title = playlist.title,
            subtitle = "YouTube Music \u2022 ${tracks.size} tracks",
            mode = "custom",
            tracks = tracks,
        )
    }

    /** Makes an owned account playlist local while retaining its live two-way mapping. */
    suspend fun importOwnedYouTubePlaylist(playlist: YouTubePlaylistResult): SavedPlaylist =
        ytMusicPreferences.playlistSyncMutex.withLock {
            withContext(Dispatchers.IO) {
                val mappings = ytMusicPreferences.mappings().toMutableMap()
                val remoteId = playlist.id.removePrefix("VL")
                val existingIds = mappings.filterValues {
                    it.remotePlaylistId.removePrefix("VL") == remoteId
                }.keys
                playlistRepository.getAll().firstOrNull { it.id in existingIds }?.let { return@withContext it }
                val saved = importYouTubePlaylist(playlist)
                mappings[saved.id] = YtPlaylistMapping(
                    remotePlaylistId = remoteId,
                    remoteTitle = playlist.title,
                    lastSyncAtMillis = System.currentTimeMillis(),
                    lastSyncedVideoIds = playlist.tracks.map { it.videoId },
                    deleteRemoteWithLocal = false,
                )
                ytMusicPreferences.setMappings(mappings)
                val selectedIds = ytMusicPreferences.syncedPlaylistIds.first()
                if (selectedIds != null && saved.id !in selectedIds) {
                    ytMusicPreferences.setSyncedPlaylistIds(selectedIds + saved.id)
                }
                saved
            }
        }

    suspend fun importCsvStream(
        inputStream: InputStream,
        filename: String,
    ): Pair<SavedPlaylist, CsvImportResult> = withContext(Dispatchers.IO) {
        val result = csvPlaylistImporter.parseAndMatchCsv(inputStream, filename)
        require(result.tracks.isNotEmpty()) {
            "No verified tracks found. Include track titles and artists, or YouTube links. Nothing was imported."
        }
        val fileType = filename.substringAfterLast('.', "File").uppercase()
        val saved = playlistRepository.save(
            title = result.suggestedTitle,
            subtitle = "$fileType Import \u2022 ${result.matchedCount} imported, ${result.totalRows - result.matchedCount} skipped",
            mode = "custom",
            tracks = result.tracks,
        )
        Pair(saved, result)
    }
}
