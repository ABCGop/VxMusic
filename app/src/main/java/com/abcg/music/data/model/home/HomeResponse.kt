package com.abcg.music.data.model.home

import androidx.compose.runtime.Immutable
import com.abcg.music.data.model.explore.mood.Mood
import com.abcg.music.data.model.home.chart.Chart
import com.abcg.music.utils.Resource

@Immutable
data class HomeResponse(
    val homeItem: Resource<ArrayList<HomeItem>>,
    val exploreMood: Resource<Mood>,
    val exploreChart: Resource<Chart>,
)