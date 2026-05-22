package com.maxrave.kotlinytmusicscraper.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SimpMusicChartResponse(
    @SerialName("data")
    val data: List<Data?>?,
    @SerialName("meta")
    val meta: Meta?,
    @SerialName("success")
    val success: Boolean?,
) {
    @Serializable
    data class Data(
        @SerialName("country")
        val country: String?,
        @SerialName("createdAt")
        val createdAt: String?,
        @SerialName("description")
        val description: String?,
        @SerialName("id")
        val id: String?,
        @SerialName("name")
        val name: String?,
        @SerialName("youtubePlaylistId")
        val youtubePlaylistId: String?,
        @SerialName("youtubeUrl")
        val youtubeUrl: String?,
    )

    @Serializable
    data class Meta(
        @SerialName("total")
        val total: Int?,
        @SerialName("weekEnd")
        val weekEnd: String?,
        @SerialName("weekStart")
        val weekStart: String?,
    )
}