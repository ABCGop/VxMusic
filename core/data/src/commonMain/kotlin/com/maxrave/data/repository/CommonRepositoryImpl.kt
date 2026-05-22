package com.maxrave.data.repository

import com.maxrave.data.db.MusicDatabase
import com.maxrave.data.db.datasource.LocalDataSource
import com.maxrave.data.io.fileSystem
import com.maxrave.domain.data.entities.NotificationEntity
import com.maxrave.domain.data.model.cookie.CookieItem
import com.maxrave.domain.data.type.RecentlyType
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.repository.CommonRepository
import com.maxrave.kotlinytmusicscraper.YouTube
import com.maxrave.kotlinytmusicscraper.models.YouTubeLocale
import com.maxrave.logger.Logger
import com.maxrave.spotify.Spotify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import org.simpmusic.aiservice.AIHost
import org.simpmusic.aiservice.AiClient
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class CommonRepositoryImpl(
    private val coroutineScope: CoroutineScope,
    private val database: MusicDatabase,
    private val localDataSource: LocalDataSource,
    private val youTube: YouTube,
    private val spotify: Spotify,
    private val aiClient: AiClient,
) : CommonRepository {
    @OptIn(ExperimentalTime::class)
    override fun init(
        cookiePath: String,
        dataStoreManager: DataStoreManager,
    ) {
        youTube.cookiePath = cookiePath.toPath()
        coroutineScope.launch {
            val resetSpotifyToken =
                launch {
                    dataStoreManager.setSpotifyClientToken("")
                    dataStoreManager.setSpotifyPersonalToken("")
                    dataStoreManager.setSpotifyClientTokenExpires(Clock.System.now().epochSeconds)
                    dataStoreManager.setSpotifyPersonalTokenExpires(Clock.System.now().epochSeconds)
                }
            val localeJob =
                launch {
                    combine(dataStoreManager.location, dataStoreManager.language) { location, language ->
                        Pair(location, language)
                    }.collectLatest { (location, language) ->
                        youTube.locale =
                            YouTubeLocale(
                                location,
                                try {
                                    language.substring(0..1)
                                } catch (e: Exception) {
                                    "en"
                                },
                            )
                    }
                }
            val ytCookieJob =
                launch {
                    dataStoreManager.cookie.distinctUntilChanged().collectLatest { cookie ->
                        if (cookie.isNotEmpty()) {
                            youTube.cookie = cookie
                            youTube.visitorData()?.let {
                                youTube.visitorData = it
                            }
                        } else {
                            youTube.cookie = null
                        }
                        Logger.d("YouTube", "New cookie")
                        localDataSource.getUsedGoogleAccount()?.netscapeCookie?.let {
                            writeTextToFile(it, cookiePath)
                            Logger.w("YouTube", "Wrote cookie to file")
                        }
                    }
                }
            val pageIdJob =
                launch {
                    dataStoreManager.pageId.distinctUntilChanged().collectLatest { pageId ->
                        youTube.pageId = pageId.ifEmpty { null }
                        Logger.d("YouTube", "New pageId")
                        localDataSource.getUsedGoogleAccount()?.netscapeCookie?.let {
                            writeTextToFile(it, cookiePath)
                            Logger.w("YouTube", "Wrote cookie to file")
                        }
                    }
                }
            val usingProxy =
                launch {
                    combine(
                        combine(
                            dataStoreManager.usingProxy,
                            dataStoreManager.proxyType,
                            dataStoreManager.proxyHost,
                            dataStoreManager.proxyPort,
                        ) { usingProxy, proxyType, proxyHost, proxyPort ->
                            (usingProxy == DataStoreManager.TRUE) to ProxyData(proxyType, proxyHost, proxyPort, "", "")
                        },
                        dataStoreManager.proxyUsername,
                        dataStoreManager.proxyPassword,
                    ) { (enabled, baseData), username, password ->
                        enabled to baseData.copy(username = username, password = password)
                    }.collectLatest { (usingProxy, data) ->
                        if (usingProxy) {
                            withContext(Dispatchers.IO) {
                                // Set SOCKS proxy authenticator if credentials are provided
                                if (data.type == DataStoreManager.ProxyType.PROXY_TYPE_SOCKS &&
                                    data.username.isNotEmpty() && data.password.isNotEmpty()
                                ) {
                                    setProxyAuthenticator(data.username, data.password)
                                } else {
                                    clearProxyAuthenticator()
                                }
                                youTube.setProxy(
                                    data.type == DataStoreManager.ProxyType.PROXY_TYPE_HTTP,
                                    data.host,
                                    data.port,
                                )
                                spotify.setProxy(
                                    data.type == DataStoreManager.ProxyType.PROXY_TYPE_HTTP,
                                    data.host,
                                    data.port,
                                )
                            }
                        } else {
                            clearProxyAuthenticator()
                            youTube.removeProxy()
                            spotify.removeProxy()
                        }
                    }
                }
            val dataSyncIdJob =
                launch {
                    dataStoreManager.dataSyncId.collectLatest { dataSyncId ->
                        youTube.dataSyncId = dataSyncId
                    }
                }
            val visitorDataJob =
                launch {
                    dataStoreManager.visitorData.collectLatest { visitorData ->
                        youTube.visitorData = visitorData
                    }
                }
            val aiClientProviderJob =
                launch {
                    dataStoreManager.aiProvider.collectLatest { provider ->
                        aiClient.host =
                            when (provider) {
                                DataStoreManager.AI_PROVIDER_GEMINI -> AIHost.GEMINI
                                DataStoreManager.AI_PROVIDER_OPENAI -> AIHost.OPENAI
                                DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI -> AIHost.CUSTOM_OPENAI
                                else -> AIHost.GEMINI // Default to Gemini if not set
                            }
                    }
                }
            val aiClientApiKeyJob =
                launch {
                    dataStoreManager.aiApiKey.collectLatest { apiKey ->
                        aiClient.apiKey =
                            apiKey.ifEmpty {
                                null
                            }
                    }
                }
            val aiCustomModelIdJob =
                launch {
                    dataStoreManager.customModelId.collectLatest { modelId ->
                        aiClient.customModelId =
                            modelId.ifEmpty {
                                null
                            }
                    }
                }
            val aiCustomBaseUrlJob =
                launch {
                    dataStoreManager.customOpenAIBaseUrl.collectLatest { baseUrl ->
                        aiClient.customBaseUrl =
                            baseUrl.ifEmpty {
                                null
                            }
                    }
                }
            val aiCustomHeadersJob =
                launch {
                    dataStoreManager.customOpenAIHeaders.collectLatest { headers ->
                        aiClient.customHeaders =
                            if (headers.isNotEmpty()) {
                                try {
                                    // Parse JSON format: {"key1":"value1","key2":"value2"}
                                    headers
                                        .trim()
                                        .removeSurrounding("{", "}")
                                        .split(",")
                                        .mapNotNull { pair ->
                                            val parts = pair.split(":")
                                            if (parts.size == 2) {
                                                parts[0].trim().removeSurrounding("\"") to
                                                    parts[1].trim().removeSurrounding("\"")
                                            } else {
                                                null
                                            }
                                        }.toMap()
                                } catch (e: Exception) {
                                    Logger.e("CommonRepository", "Failed to parse custom headers: ${e.message}")
                                    null
                                }
                            } else {
                                null
                            }
                    }
                }

            localeJob.join()
            ytCookieJob.join()
            pageIdJob.join()
            usingProxy.join()
            dataSyncIdJob.join()
            visitorDataJob.join()
            resetSpotifyToken.join()
            aiClientProviderJob.join()
            aiClientApiKeyJob.join()
            aiCustomModelIdJob.join()
            aiCustomBaseUrlJob.join()
            aiCustomHeadersJob.join()
        }
    }

    // Database
    override fun closeDatabase() {
        database.close()
    }

    override fun getDatabasePath() =
        com.maxrave.data.db
            .getDatabasePath()

    override suspend fun databaseDaoCheckpoint() = localDataSource.checkpoint()

    // Recently data
    override fun getAllRecentData(): Flow<List<RecentlyType>> =
        flow {
            emit(localDataSource.getAllRecentData())
        }.flowOn(Dispatchers.IO)

    // Notifications
    override suspend fun insertNotification(notificationEntity: NotificationEntity) =
        withContext(Dispatchers.IO) {
            localDataSource.insertNotification(notificationEntity)
        }

    override suspend fun getAllNotifications(): Flow<List<NotificationEntity>?> =
        flow {
            emit(localDataSource.getAllNotification())
        }.flowOn(Dispatchers.IO)

    override suspend fun deleteNotification(id: Long) =
        withContext(Dispatchers.IO) {
            localDataSource.deleteNotification(id)
        }

    override suspend fun writeTextToFile(
        text: String,
        filePath: String,
    ): Boolean {
        try {
            fileSystem().sink(filePath.toPath()).buffer().use { sink ->
                sink.writeUtf8(text)
                sink.close()
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Original from YTDLnis app
     */
    override suspend fun getCookiesFromInternalDatabase(
        url: String,
        packageName: String,
    ): CookieItem =
        withContext(Dispatchers.IO) {
            return@withContext getCookies(
                url,
                packageName,
            )
        }
}

private data class ProxyData(
    val type: DataStoreManager.ProxyType,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
)

expect fun setProxyAuthenticator(username: String, password: String)

expect fun clearProxyAuthenticator()

expect fun getCookies(
    url: String,
    packageName: String,
): CookieItem