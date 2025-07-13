package com.abcg.music.data.model.browse.artist

import com.google.gson.annotations.SerializedName
import com.abcg.music.data.model.searchResult.songs.Thumbnail
import com.abcg.music.data.type.HomeContentType

data class ResultAlbum(
    @SerializedName("browseId")
    val browseId: String,
    @SerializedName("isExplicit")
    val isExplicit: Boolean,
    @SerializedName("thumbnails")
    val thumbnails: List<Thumbnail>,
    @SerializedName("title")
    val title: String,
    @SerializedName("year")
    val year: String,
) : HomeContentType