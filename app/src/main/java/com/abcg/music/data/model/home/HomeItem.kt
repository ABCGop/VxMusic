package com.abcg.music.data.model.home

import androidx.compose.runtime.Immutable
import com.abcg.music.data.model.searchResult.songs.Thumbnail

@Immutable
data class HomeItem(
    val contents: List<Content?>,
    val title: String,
    val subtitle: String? = null,
    val thumbnail: List<Thumbnail>? = null,
    val channelId: String? = null,
)