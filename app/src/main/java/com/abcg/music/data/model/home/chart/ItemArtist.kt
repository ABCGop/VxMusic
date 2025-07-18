package com.abcg.music.data.model.home.chart

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import com.abcg.music.data.model.searchResult.songs.Thumbnail

@Immutable
data class ItemArtist(
    @SerializedName("browseId")
    val browseId: String,
    @SerializedName("rank")
    val rank: String,
    @SerializedName("subscribers")
    val subscribers: String,
    @SerializedName("thumbnails")
    val thumbnails: List<Thumbnail>,
    @SerializedName("title")
    val title: String,
    @SerializedName("trend")
    val trend: String,
)