package com.abcg.music.data.jiosaavn

import kotlinx.serialization.Serializable

@Serializable
data class JioSaavnAudioStream(
    val url: String,
    val mimeType: String = "audio/mp4",
    val bitrateKbps: Int = 320,
    val durationSeconds: Int = 0,
    val songId: String = "",
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val artworkUrl: String? = null,
)
