package com.abcg.music.data.model.explore.mood.genre

import androidx.compose.runtime.Immutable
import com.abcg.music.data.model.searchResult.songs.Artist

@Immutable
data class ItemsSong(
    val title: String,
    val artist: List<Artist>?,
    val videoId: String,
)