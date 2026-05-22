package com.maxrave.domain.data.model.metadata

import kotlinx.serialization.Serializable

@Serializable
data class Lyrics(
    val error: Boolean = false,
    val lines: List<Line>?,
    val syncType: String?,
    val simpMusicLyrics: SimpMusicLyrics? = null,
)

@Serializable
data class SimpMusicLyrics(
    val id: String,
    val vote: Int,
)