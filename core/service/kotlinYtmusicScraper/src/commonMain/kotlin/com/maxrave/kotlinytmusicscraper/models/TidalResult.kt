package com.maxrave.kotlinytmusicscraper.models

import com.maxrave.kotlinytmusicscraper.models.response.TidalStreamResponse

/**
 * Result of a Tidal stream search, including stream data and audio analysis metadata.
 */
data class TidalStreamResult(
    val stream: TidalStreamResponse,
    val bpm: Int?,
    val musicKey: String?,
    val keyScale: String?,
)

/**
 * Result of a Tidal metadata search (search only, no stream fetching).
 */
data class TidalMetadataResult(
    val bpm: Int?,
    val musicKey: String?,
    val keyScale: String?,
)
