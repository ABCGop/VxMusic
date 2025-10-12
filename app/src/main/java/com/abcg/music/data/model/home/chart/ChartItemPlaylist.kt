package com.abcg.music.data.model.home.chart

import androidx.compose.runtime.Immutable
import com.abcg.music.data.model.browse.artist.ResultPlaylist

@Immutable
data class ChartItemPlaylist(
    val title: String,
    val playlists: List<ResultPlaylist>,
)