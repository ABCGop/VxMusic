package com.abcg.kotlinytmusicscraper.pages

import com.abcg.kotlinytmusicscraper.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)