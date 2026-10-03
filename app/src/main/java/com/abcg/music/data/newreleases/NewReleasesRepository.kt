package com.abcg.music.data.newreleases

import com.abcg.music.data.music.InnerTubeMusicApi
import com.abcg.music.data.music.YouTubeMusicTrack
import com.abcg.music.data.music.YouTubePlaylistSummary
import com.abcg.music.playback.PlayableTrack
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.ArrayDeque
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewReleasesRepository @Inject constructor(
    private val innerTube: InnerTubeMusicApi,
) {
    private val mutex = Mutex()
    private val albumQueue = ArrayDeque<YouTubePlaylistSummary>()
    private val seenAlbumIds = mutableSetOf<String>()
    private val seenVideoIds = mutableSetOf<String>()
    private val seenTokens = mutableSetOf<String>()
    private var exploreToken: String? = null
    private var albumsToken: String? = null
    private var searchIndex = 0
    private var gridLoaded = false
    private val searchQueries: List<String> = run {
            val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            listOf("new music $year", "new songs $year", "new music friday",
                "latest releases $year", "new pop hits $year", "new hip hop $year",
                "new indie $year", "new rock $year")
        }

    val hasMore: Boolean
        get() = !gridLoaded || albumQueue.isNotEmpty() || exploreToken != null || albumsToken != null || searchIndex < searchQueries.size

    private fun clear() {
        albumQueue.clear()
        seenAlbumIds.clear()
        seenVideoIds.clear()
        seenTokens.clear()
        exploreToken = null
        albumsToken = null
        searchIndex = 0
        gridLoaded = false
    }

    suspend fun reset() = mutex.withLock { clear() }

    suspend fun fetchInitialBatch(): List<YouTubeMusicTrack> = mutex.withLock {
        clear()
        val batch = innerTube.fetchNewReleasesPage()
        exploreToken = batch.continuationToken
        enqueue(batch.albums)
        val direct = unique(batch.directTracks)
        if (direct.isNotEmpty()) return@withLock direct
        if (albumQueue.isEmpty()) {
            val grid = innerTube.fetchNewReleasesAlbumsGrid()
            gridLoaded = true
            albumsToken = grid.second
            enqueue(grid.first)
        }
        nextBatch()
    }

    suspend fun fetchNextBatch(): List<YouTubeMusicTrack> = mutex.withLock {
        nextBatch()
    }

    private suspend fun nextBatch(): List<YouTubeMusicTrack> {
        while (hasMore) {
            if (albumQueue.isNotEmpty()) {
                val album = albumQueue.first
                val page = innerTube.fetchAlbumPage(album.id, album.title, album.author.orEmpty())
                    ?: throw java.io.IOException("Couldn't load ${album.title}. Tap Retry.")
                val tracks = page.tracks.mapNotNull { it.toYouTubeMusicTrack() }
                albumQueue.removeFirst()
                val fresh = unique(tracks)
                if (fresh.isNotEmpty()) return fresh
                continue
            }
            if (!gridLoaded) {
                val grid = innerTube.fetchNewReleasesAlbumsGrid()
                gridLoaded = true
                albumsToken = grid.second
                enqueue(grid.first)
                continue
            }
            albumsToken?.let { token ->
                val batch = innerTube.fetchNewReleasesAlbumsGrid(token)
                seenTokens.add(token)
                albumsToken = batch.second?.takeUnless { it in seenTokens }
                enqueue(batch.first)
            } ?: exploreToken?.let { token ->
                val batch = innerTube.fetchNewReleasesPage(token)
                seenTokens.add(token)
                exploreToken = batch.continuationToken?.takeUnless { it in seenTokens }
                enqueue(batch.albums)
                val fresh = unique(batch.directTracks)
                if (fresh.isNotEmpty()) return fresh
            } ?: run {
                val query = searchQueries.getOrNull(searchIndex) ?: return emptyList()
                val tracks = innerTube.searchSongs(query, limit = 30, prefetchStreams = false)
                searchIndex++
                val fresh = unique(tracks)
                if (fresh.isNotEmpty()) return fresh
            }
        }
        return emptyList()
    }

    private fun enqueue(albums: List<YouTubePlaylistSummary>) {
        albums.filter {
            (it.id.startsWith("MPRE") || it.id.startsWith("PL") || it.id.startsWith("OLAK") || it.id.startsWith("VL")) &&
                seenAlbumIds.add(it.id)
        }.forEach(albumQueue::addLast)
    }

    private fun unique(tracks: List<YouTubeMusicTrack>) =
        tracks.filter { it.videoId.isNotBlank() && seenVideoIds.add(it.videoId) }

    private fun PlayableTrack.toYouTubeMusicTrack(): YouTubeMusicTrack? {
        val id = videoId?.takeIf(String::isNotBlank) ?: return null
        return YouTubeMusicTrack(videoId = id, title = title, artist = artist,
            album = album, artworkUrl = artworkUrl, durationSeconds = null)
    }
}
