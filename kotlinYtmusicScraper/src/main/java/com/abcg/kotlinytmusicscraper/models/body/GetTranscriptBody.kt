package com.abcg.kotlinytmusicscraper.models.body

import com.abcg.kotlinytmusicscraper.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)