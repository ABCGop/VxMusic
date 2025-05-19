package com.abcg.music.models

import com.abcg.innertube.models.YTItem
import com.abcg.music.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
