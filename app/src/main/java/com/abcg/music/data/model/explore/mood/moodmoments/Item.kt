package com.abcg.music.data.model.explore.mood.moodmoments

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class Item(
    @SerializedName("contents")
    val contents: List<Content>,
    @SerializedName("header")
    val header: String,
)