package com.maxrave.kotlinytmusicscraper.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TidalSearchResponse(
    @SerialName("data")
    val data: Data?,
    @SerialName("version")
    val version: String?,
) {
    @Serializable
    data class Data(
        @SerialName("items")
        val items: List<Item?>?,
        @SerialName("limit")
        val limit: Int?,
        @SerialName("offset")
        val offset: Int?,
        @SerialName("totalNumberOfItems")
        val totalNumberOfItems: Int?,
    ) {
        @Serializable
        data class Item(
            @SerialName("accessType")
            val accessType: String?,
            @SerialName("adSupportedStreamReady")
            val adSupportedStreamReady: Boolean?,
            @SerialName("album")
            val album: Album?,
            @SerialName("allowStreaming")
            val allowStreaming: Boolean?,
            @SerialName("artist")
            val artist: Artist?,
            @SerialName("artists")
            val artists: List<Artist?>?,
            @SerialName("audioModes")
            val audioModes: List<String?>?,
            @SerialName("audioQuality")
            val audioQuality: String?,
            @SerialName("bpm")
            val bpm: Int?,
            @SerialName("copyright")
            val copyright: String?,
            @SerialName("djReady")
            val djReady: Boolean?,
            @SerialName("duration")
            val duration: Int?,
            @SerialName("editable")
            val editable: Boolean?,
            @SerialName("explicit")
            val explicit: Boolean?,
            @SerialName("id")
            val id: Int?,
            @SerialName("isrc")
            val isrc: String?,
            @SerialName("key")
            val key: String?,
            @SerialName("keyScale")
            val keyScale: String?,
            @SerialName("mediaMetadata")
            val mediaMetadata: MediaMetadata?,
            @SerialName("mixes")
            val mixes: Mixes?,
            @SerialName("payToStream")
            val payToStream: Boolean?,
            @SerialName("peak")
            val peak: Double?,
            @SerialName("popularity")
            val popularity: Int?,
            @SerialName("premiumStreamingOnly")
            val premiumStreamingOnly: Boolean?,
            @SerialName("replayGain")
            val replayGain: Double?,
            @SerialName("spotlighted")
            val spotlighted: Boolean?,
            @SerialName("stemReady")
            val stemReady: Boolean?,
            @SerialName("streamReady")
            val streamReady: Boolean?,
            @SerialName("streamStartDate")
            val streamStartDate: String?,
            @SerialName("title")
            val title: String?,
            @SerialName("trackNumber")
            val trackNumber: Int?,
            @SerialName("upload")
            val upload: Boolean?,
            @SerialName("url")
            val url: String?,
            @SerialName("version")
            val version: String?,
            @SerialName("volumeNumber")
            val volumeNumber: Int?,
        ) {
            @Serializable
            data class Album(
                @SerialName("cover")
                val cover: String?,
                @SerialName("id")
                val id: Int?,
                @SerialName("title")
                val title: String?,
                @SerialName("vibrantColor")
                val vibrantColor: String?,
                @SerialName("videoCover")
                val videoCover: String?,
            )

            @Serializable
            data class Artist(
                @SerialName("handle")
                val handle: String?,
                @SerialName("id")
                val id: Int?,
                @SerialName("name")
                val name: String?,
                @SerialName("picture")
                val picture: String?,
                @SerialName("type")
                val type: String?,
            )

            @Serializable
            data class MediaMetadata(
                @SerialName("tags")
                val tags: List<String?>?,
            )

            @Serializable
            data class Mixes(
                @SerialName("TRACK_MIX")
                val tRACKMIX: String?,
            )
        }
    }
}