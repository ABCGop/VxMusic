@file:OptIn(ExperimentalCoroutinesApi::class)

package com.abcg.music.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.Download
import com.abcg.innertube.YouTube
import com.abcg.music.constants.AlbumFilter
import com.abcg.music.constants.AlbumFilterKey
import com.abcg.music.constants.AlbumSortDescendingKey
import com.abcg.music.constants.AlbumSortType
import com.abcg.music.constants.AlbumSortTypeKey
import com.abcg.music.constants.ArtistFilter
import com.abcg.music.constants.ArtistFilterKey
import com.abcg.music.constants.ArtistSongSortDescendingKey
import com.abcg.music.constants.ArtistSongSortType
import com.abcg.music.constants.ArtistSongSortTypeKey
import com.abcg.music.constants.ArtistSortDescendingKey
import com.abcg.music.constants.ArtistSortType
import com.abcg.music.constants.ArtistSortTypeKey
import com.abcg.music.constants.PlaylistSortDescendingKey
import com.abcg.music.constants.PlaylistSortType
import com.abcg.music.constants.PlaylistSortTypeKey
import com.abcg.music.constants.SongFilter
import com.abcg.music.constants.SongFilterKey
import com.abcg.music.constants.SongSortDescendingKey
import com.abcg.music.constants.SongSortType
import com.abcg.music.constants.SongSortTypeKey
import com.abcg.music.db.MusicDatabase
import com.abcg.music.extensions.reversed
import com.abcg.music.extensions.toEnum
import com.abcg.music.playback.DownloadUtil
import com.abcg.music.utils.dataStore
import com.abcg.music.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class LibrarySongsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    downloadUtil: DownloadUtil,
) : ViewModel() {
    val allSongs = context.dataStore.data
        .map {
            Triple(
                it[SongFilterKey].toEnum(SongFilter.LIBRARY),
                it[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE),
                (it[SongSortDescendingKey] ?: true)
            )
        }
        .distinctUntilChanged()
        .flatMapLatest { (filter, sortType, descending) ->
            when (filter) {
                SongFilter.LIBRARY -> database.songs(sortType, descending)
                SongFilter.LIKED -> database.likedSongs(sortType, descending)
                SongFilter.DOWNLOADED -> downloadUtil.downloads.flatMapLatest { downloads ->
                    database.allSongs()
                        .flowOn(Dispatchers.IO)
                        .map { songs ->
                            songs.filter {
                                downloads[it.id]?.state == Download.STATE_COMPLETED
                            }
                        }
                        .map { songs ->
                            when (sortType) {
                                SongSortType.CREATE_DATE -> songs.sortedBy { downloads[it.id]?.updateTimeMs ?: 0L }
                                SongSortType.NAME -> songs.sortedBy { it.song.title }
                                SongSortType.ARTIST -> songs.sortedBy { song ->
                                    song.artists.joinToString(separator = "") { it.name }
                                }

                                SongSortType.PLAY_TIME -> songs.sortedBy { it.song.totalPlayTime }
                            }.reversed(descending)
                        }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)
}

@HiltViewModel
class LibraryArtistsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
) : ViewModel() {
    val allArtists = context.dataStore.data
        .map {
            Triple(
                it[ArtistFilterKey].toEnum(ArtistFilter.LIBRARY),
                it[ArtistSortTypeKey].toEnum(ArtistSortType.CREATE_DATE),
                it[ArtistSortDescendingKey] ?: true
            )
        }
        .distinctUntilChanged()
        .flatMapLatest { (filter, sortType, descending) ->
            when (filter) {
                ArtistFilter.LIBRARY -> database.artists(sortType, descending)
                ArtistFilter.LIKED -> database.artistsBookmarked(sortType, descending)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allArtists.collect { artists ->
                artists
                    ?.map { it.artist }
                    ?.filter {
                        it.thumbnailUrl == null || Duration.between(it.lastUpdateTime, LocalDateTime.now()) > Duration.ofDays(10)
                    }
                    ?.forEach { artist ->
                        YouTube.artist(artist.id).onSuccess { artistPage ->
                            database.query {
                                update(artist, artistPage)
                            }
                        }
                    }
            }
        }
    }
}

@HiltViewModel
class LibraryAlbumsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
) : ViewModel() {
    val allAlbums = context.dataStore.data
        .map {
            Triple(
                it[AlbumFilterKey].toEnum(AlbumFilter.LIBRARY),
                it[AlbumSortTypeKey].toEnum(AlbumSortType.CREATE_DATE),
                it[AlbumSortDescendingKey] ?: true
            )
        }
        .distinctUntilChanged()
        .flatMapLatest { (filter, sortType, descending) ->
            when (filter) {
                AlbumFilter.LIBRARY -> database.albums(sortType, descending)
                AlbumFilter.LIKED -> database.albumsLiked(sortType, descending)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            allAlbums.collect { albums ->
                albums
                    ?.filter {
                        it.album.songCount == 0
                    }
                    ?.forEach { album ->
                        YouTube.album(album.id).onSuccess { albumPage ->
                            database.query {
                                update(album.album, albumPage)
                            }
                        }.onFailure {
                            reportException(it)
                            if (it.message?.contains("NOT_FOUND") == true) {
                                database.query {
                                    delete(album.album)
                                }
                            }
                        }
                    }
            }
        }
    }
}

@HiltViewModel
class LibraryPlaylistsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
) : ViewModel() {
    val allPlaylists = context.dataStore.data
        .map {
            it[PlaylistSortTypeKey].toEnum(PlaylistSortType.CREATE_DATE) to (it[PlaylistSortDescendingKey] ?: true)
        }
        .distinctUntilChanged()
        .flatMapLatest { (sortType, descending) ->
            database.playlists(sortType, descending)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
}

@HiltViewModel
class ArtistSongsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val artistId = savedStateHandle.get<String>("artistId")!!
    val artist = database.artist(artistId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val songs = context.dataStore.data
        .map {
            it[ArtistSongSortTypeKey].toEnum(ArtistSongSortType.CREATE_DATE) to (it[ArtistSongSortDescendingKey] ?: true)
        }
        .distinctUntilChanged()
        .flatMapLatest { (sortType, descending) ->
            database.artistSongs(artistId, sortType, descending)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
