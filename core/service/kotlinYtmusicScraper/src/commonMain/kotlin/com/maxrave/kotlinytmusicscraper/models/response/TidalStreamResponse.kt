package com.maxrave.kotlinytmusicscraper.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
data class TidalStreamResponse(
    @SerialName("data")
    val data: Data?,
    @SerialName("version")
    val version: String?,
) {
    @Serializable
    data class Data(
        @SerialName("albumPeakAmplitude")
        val albumPeakAmplitude: Double?,
        @SerialName("albumReplayGain")
        val albumReplayGain: Double?,
        @SerialName("assetPresentation")
        val assetPresentation: String?,
        @SerialName("audioMode")
        val audioMode: String?,
        @SerialName("audioQuality")
        val audioQuality: String?,
        @SerialName("bitDepth")
        val bitDepth: Int?,
        @SerialName("manifest")
        val manifest: String?,
        @SerialName("manifestHash")
        val manifestHash: String?,
        @SerialName("manifestMimeType")
        val manifestMimeType: String?,
        @SerialName("sampleRate")
        val sampleRate: Int?,
        @SerialName("trackId")
        val trackId: Int?,
        @SerialName("trackPeakAmplitude")
        val trackPeakAmplitude: Double?,
        @SerialName("trackReplayGain")
        val trackReplayGain: Double?,
    )
}

@Serializable
data class AudioData(
    val mimeType: String,
    val codecs: String,
    val encryptionType: String,
    val urls: List<String>,
)