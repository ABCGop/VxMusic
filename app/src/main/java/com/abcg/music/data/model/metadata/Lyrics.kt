package com.abcg.music.data.model.metadata

import com.google.gson.annotations.SerializedName

data class Lyrics(
    @SerializedName("error")
    val error: Boolean,
    @SerializedName("lines")
    val lines: List<Line>?,
    @SerializedName("syncType")
    val syncType: String?,
    val captchaRequired: Boolean = false,
)