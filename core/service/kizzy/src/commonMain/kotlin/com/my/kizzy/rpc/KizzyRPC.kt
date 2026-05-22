/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * KizzyRPC.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.rpc

import com.my.kizzy.gateway.DiscordWebSocket
import com.my.kizzy.gateway.entities.MeResponse
import com.my.kizzy.gateway.entities.presence.Activity
import com.my.kizzy.gateway.entities.presence.Assets
import com.my.kizzy.gateway.entities.presence.Metadata
import com.my.kizzy.gateway.entities.presence.Presence
import com.my.kizzy.gateway.entities.presence.Timestamps
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

/**
 * Modified by Zion Huang
 */
open class KizzyRPC(
    private val token: String,
    os: String = "Android",
    browser: String = "Discord Android",
    device: String = "Generic Android Device",
    private val userAgent: String = "Discord-Android/314013;RNA",
    private val superPropertiesBase64: String? = null,
) {
    private val discordWebSocket = DiscordWebSocket(token, os, browser, device)
    private val discordApiClient = HttpClient()

    fun closeRPC() {
        discordWebSocket.close()
    }

    fun isRpcRunning(): Boolean = discordWebSocket.isWebSocketConnected()

    open suspend fun close() {
        if (!isRpcRunning()) {
            discordWebSocket.connect()
        }
        val presence =
            Presence(
                activities = emptyList(),
            )
        discordWebSocket.sendActivity(presence)
    }

    suspend fun setActivity(
        name: String,
        state: String?,
        stateUrl: String? = null,
        details: String?,
        detailsUrl: String? = null,
        largeImage: RpcImage?,
        smallImage: RpcImage?,
        largeText: String? = null,
        smallText: String? = null,
        buttons: List<Pair<String, String>>? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        type: Type = Type.LISTENING,
        statusDisplayType: StatusDisplayType = StatusDisplayType.NAME,
        streamUrl: String? = null,
        applicationId: String? = null,
        status: String? = "online",
        since: Long? = null,
    ) {
        if (!isRpcRunning()) {
            discordWebSocket.connect()
        }

        val resolveExternal: suspend (String) -> String? = { image ->
            if (applicationId.isNullOrBlank()) {
                null
            } else {
                fetchExternalAsset(
                    client = discordApiClient,
                    applicationId = applicationId,
                    token = token,
                    imageUrl = image,
                    userAgent = userAgent,
                    superPropertiesBase64 = superPropertiesBase64,
                )
            }
        }

        val presence =
            Presence(
                activities =
                    listOf(
                        Activity(
                            name = name,
                            state = state,
                            stateUrl = stateUrl,
                            details = details,
                            detailsUrl = detailsUrl,
                            type = type.value,
                            statusDisplayType = statusDisplayType.value,
                            timestamps = Timestamps(startTime, endTime),
                            assets =
                                Assets(
                                    largeImage = largeImage?.resolveImage(resolveExternal),
                                    smallImage = smallImage?.resolveImage(resolveExternal),
                                    largeText = largeText,
                                    smallText = smallText,
                                ),
                            buttons = buttons?.map { it.first },
                            metadata = Metadata(buttonUrls = buttons?.map { it.second }),
                            applicationId = applicationId.takeIf { !buttons.isNullOrEmpty() },
                            url = streamUrl,
                        ),
                    ),
                afk = true,
                since = since,
                status = status ?: "online",
            )
        discordWebSocket.sendActivity(presence)
    }

    enum class Type(
        val value: Int,
    ) {
        PLAYING(0),
        STREAMING(1),
        LISTENING(2),
        WATCHING(3),
        COMPETING(5),
    }

    enum class StatusDisplayType(
        val value: Int,
    ) {
        NAME(0),
        STATE(1),
        DETAILS(2),
    }

    companion object {
        suspend fun getUserInfo(token: String): Result<UserInfo> =
            runCatching {
                val client = HttpClient()
                val rawRes =
                    client
                        .get("https://discord.com/api/v9/users/@me") {
                            header("Authorization", token)
                        }.bodyAsText()
                val json =
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                val response = json.decodeFromString<MeResponse>(rawRes)
                val username = response.username ?: "Unknown"
                val name = response.globalName ?: username
                val avatar = response.avatar
                client.close()

                UserInfo(username, username, name, avatar)
            }
    }
}