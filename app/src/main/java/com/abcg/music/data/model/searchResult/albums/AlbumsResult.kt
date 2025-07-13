package com.abcg.music.data.model.searchResult.albums

import com.google.gson.annotations.SerializedName
import com.abcg.music.data.model.searchResult.songs.Artist
import com.abcg.music.data.model.searchResult.songs.Thumbnail
import com.abcg.music.data.type.PlaylistType

data class AlbumsResult(
    @SerializedName("artists")
    val artists: List<Artist>,
    @SerializedName("browseId")
    val browseId: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("duration")
    val duration: Any,
    @SerializedName("isExplicit")
    val isExplicit: Boolean,
    @SerializedName("resultType")
    val resultType: String,
    @SerializedName("thumbnails")
    val thumbnails: List<Thumbnail>,
    @SerializedName("title")
    val title: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("year")
    val year: String,
): PlaylistType {
    override fun playlistType(): PlaylistType.Type = PlaylistType.Type.ALBUM
}