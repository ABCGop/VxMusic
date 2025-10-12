package com.abcg.music.data.model.browse.playlist

import androidx.compose.runtime.Immutable
import com.abcg.music.data.model.searchResult.songs.Artist
import com.abcg.music.data.model.searchResult.songs.Thumbnail

@Immutable
data class TrackPlaylist(
    val albumPlaylist: AlbumPlaylist,
    val artistPlaylists: List<Artist>?,
    val duration: String,
    val durationSeconds: Int,
    val isAvailable: Boolean,
    val isExplicit: Boolean,
    val likeStatus: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val videoId: String,
    val videoType: String,
)