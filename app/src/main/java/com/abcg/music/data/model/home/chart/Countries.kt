package com.abcg.music.data.model.home.chart

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class Countries(
    @SerializedName("options")
    val options: List<String>,
    @SerializedName("selected")
    val selected: Selected,
)