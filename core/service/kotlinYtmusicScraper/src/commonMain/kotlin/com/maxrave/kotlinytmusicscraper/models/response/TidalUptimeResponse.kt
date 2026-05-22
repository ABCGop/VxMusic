package com.maxrave.kotlinytmusicscraper.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TidalUptimeResponse(
    @SerialName("lastUpdated")
    val lastUpdated: String? = null,
    @SerialName("api")
    val api: List<Server> = emptyList(),
    @SerialName("streaming")
    val streaming: List<Server> = emptyList(),
    @SerialName("down")
    val down: List<DownServer> = emptyList(),
) {
    @Serializable
    data class Server(
        @SerialName("url")
        val url: String,
        @SerialName("version")
        val version: String? = null,
    )

    @Serializable
    data class DownServer(
        @SerialName("url")
        val url: String,
        @SerialName("status")
        val status: Int? = null,
        @SerialName("error")
        val error: String? = null,
    )
}
