package com.abcg.kotlinytmusicscraper.pages

import com.abcg.kotlinytmusicscraper.models.PlaylistItem
import com.abcg.kotlinytmusicscraper.models.VideoItem

data class ExplorePage(
    val released: List<PlaylistItem>,
    val musicVideo: List<VideoItem>,
)