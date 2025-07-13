package com.abcg.music.data.model.home

import androidx.compose.runtime.Immutable
import com.abcg.music.data.model.explore.mood.Mood
import com.abcg.music.data.model.home.chart.Chart
import com.abcg.music.utils.Resource

@Immutable
data class HomeDataCombine(
    val home: Resource<ArrayList<HomeItem>>,
    val mood: Resource<Mood>,
    val chart: Resource<Chart>,
    val newRelease: Resource<ArrayList<HomeItem>>,
)