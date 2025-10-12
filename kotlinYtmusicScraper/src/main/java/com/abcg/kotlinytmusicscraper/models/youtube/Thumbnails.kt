package com.abcg.kotlinytmusicscraper.models.youtube

import com.abcg.kotlinytmusicscraper.models.Thumbnail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Thumbnails(
    @SerialName("thumbnails")
    val thumbnails: List<Thumbnail>? = null,
)