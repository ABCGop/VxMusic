package com.abcg.music.data.model.home.chart

import androidx.compose.runtime.Immutable
import com.abcg.music.data.model.searchResult.songs.Thumbnail

@Immutable
data class ItemArtist(
    val browseId: String,
    val rank: String,
    val subscribers: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val trend: String,
)