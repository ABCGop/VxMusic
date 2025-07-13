package com.abcg.music.data.model.home.chart

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class Videos(
    @SerializedName("items")
    val items: ArrayList<ItemVideo>,
    @SerializedName("playlist")
    val playlist: String,
)