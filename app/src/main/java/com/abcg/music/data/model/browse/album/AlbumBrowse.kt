package com.abcg.music.data.model.browse.album

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import com.abcg.music.data.model.browse.artist.ResultAlbum
import com.abcg.music.data.model.searchResult.songs.Artist
import com.abcg.music.data.model.searchResult.songs.Thumbnail

@Immutable
data class AlbumBrowse(
    @SerializedName("artists")
    val artists: List<Artist>,
    @SerializedName("audioPlaylistId")
    val audioPlaylistId: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("duration")
    val duration: String?,
    @SerializedName("duration_seconds")
    val durationSeconds: Int,
    @SerializedName("thumbnails")
    val thumbnails: List<Thumbnail>?,
    @SerializedName("title")
    val title: String,
    @SerializedName("trackCount")
    val trackCount: Int,
    @SerializedName("tracks")
    val tracks: List<Track>,
    @SerializedName("type")
    val type: String,
    @SerializedName("year")
    val year: String?,
    val otherVersion: List<ResultAlbum> = emptyList(),
)