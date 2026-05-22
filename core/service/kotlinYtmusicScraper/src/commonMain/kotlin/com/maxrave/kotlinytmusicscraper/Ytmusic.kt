package com.maxrave.kotlinytmusicscraper

import com.maxrave.domain.extension.now
import com.maxrave.kotlinytmusicscraper.extractor.Extractor
import com.maxrave.kotlinytmusicscraper.models.Context
import com.maxrave.kotlinytmusicscraper.models.SongItem
import com.maxrave.kotlinytmusicscraper.models.WatchEndpoint
import com.maxrave.kotlinytmusicscraper.models.YouTubeClient
import com.maxrave.kotlinytmusicscraper.models.YouTubeClient.Companion.IOS
import com.maxrave.kotlinytmusicscraper.models.YouTubeClient.Companion.TVHTML5
import com.maxrave.kotlinytmusicscraper.models.YouTubeClient.Companion.WEB_REMIX
import com.maxrave.kotlinytmusicscraper.models.YouTubeLocale
import com.maxrave.kotlinytmusicscraper.models.body.AccountMenuBody
import com.maxrave.kotlinytmusicscraper.models.body.BrowseBody
import com.maxrave.kotlinytmusicscraper.models.body.CreatePlaylistBody
import com.maxrave.kotlinytmusicscraper.models.body.EditPlaylistBody
import com.maxrave.kotlinytmusicscraper.models.body.FormData
import com.maxrave.kotlinytmusicscraper.models.body.GetQueueBody
import com.maxrave.kotlinytmusicscraper.models.body.GetSearchSuggestionsBody
import com.maxrave.kotlinytmusicscraper.models.body.LikeBody
import com.maxrave.kotlinytmusicscraper.models.body.NextBody
import com.maxrave.kotlinytmusicscraper.models.body.PlayerBody
import com.maxrave.kotlinytmusicscraper.models.body.SearchBody
import com.maxrave.kotlinytmusicscraper.models.response.DownloadProgress
import com.maxrave.kotlinytmusicscraper.utils.parseCookieString
import com.maxrave.kotlinytmusicscraper.utils.sha1
import com.maxrave.ktorext.encoding.brotli
import com.maxrave.ktorext.getEngine
import com.maxrave.logger.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.serialization.kotlinx.xml.xml
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.io.readByteArray
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use
import kotlin.time.ExperimentalTime

private const val TAG = "YouTubeScraperClient"
private const val DOWNLOAD_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
private const val DEFAULT_PARALLEL_DOWNLOADS = 4
private const val CHUNK_SIZE = 1024L * 1024L // 1MB per chunk

class Ytmusic {
    val normalJson =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
        }
    private var httpClient = createClient()
    private var downloadClient: HttpClient? = null

    private fun getDownloadClient(): HttpClient =
        downloadClient ?: HttpClient(getEngine()) {
            expectSuccess = true
            install(HttpRedirect) {
                checkHttpMethod = false
                allowHttpsDowngrade = true
            }
            // Disable logging for download - significantly improves performance
            // Note: Don't install Logging plugin for download client
        }.also { downloadClient = it }

    var cookiePath: Path? = null

    var locale =
        YouTubeLocale(
            gl = getCountry(),
            hl = getLanguage(),
        )
    var visitorData: String? = null
    var dataSyncId: String? = null
    private var poTokenChallengeRequestKey = "O43z0dpjhgX20SCx4KAo"
    var cookie: String? = null
        set(value) {
            field = value
            cookieMap = if (value == null) emptyMap() else parseCookieString(value)
            extractor.logIn(value)
        }

    var pageId: String? = null

    private var cookieMap = emptyMap<String, String>()

    var proxy: ProxyConfig? = null
        set(value) {
            field = value
            httpClient.close()
            httpClient = createClient()
        }

    private val extractor = Extractor()

    init {
        extractor.init()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createClient() =
        HttpClient(getEngine()) {
            expectSuccess = true
//            install(KtorToCurl) {
//                converter =
//                    object : CurlLogger {
//                        override fun log(curl: String) {
//                            Logger.d(TAG, "Curl command:")
//                            Logger.d(TAG, curl)
//                        }
//                    }
//            }
            install(HttpRedirect) {
                checkHttpMethod = false
                allowHttpsDowngrade = true
            }
            install(Logging) {
                logger = io.ktor.client.plugins.logging.Logger.DEFAULT
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                protobuf()
                json(
                    normalJson,
                )
                xml(
                    format =
                        XML {
                            xmlDeclMode = XmlDeclMode.Charset
                            autoPolymorphic = true
                        },
                    contentType = ContentType.Text.Xml,
                )
            }

            install(ContentEncoding) {
                brotli(1.0F)
                gzip(0.9F)
                deflate(0.8F)
            }

            if (proxy != null) {
                engine {
                    proxy = this@Ytmusic.proxy
                }
            }

            defaultRequest {
                url("https://music.youtube.com/youtubei/v1/")
            }
        }

    internal fun HttpRequestBuilder.mask(value: String = "*") = header("X-Goog-FieldMask", value)

    @OptIn(ExperimentalTime::class)
    private fun HttpRequestBuilder.ytClient(
        client: YouTubeClient,
        setLogin: Boolean = false,
        isUsingReferer: Boolean = true,
        customCookie: String? = null,
    ) {
        contentType(ContentType.Application.Json)
        headers {
            append("X-Goog-Api-Format-Version", "1")
            append("X-YouTube-Client-Name", "${client.xClientName ?: 1}")
            append("X-YouTube-Client-Version", client.clientVersion)
            append("X-Goog-Authuser", "0")
            pageId?.let {
                append("X-Goog-Pageid", it)
            }
            append("x-origin", "https://music.youtube.com")
            if (client.referer != null && isUsingReferer) {
                append("Referer", client.referer)
            }
            if (setLogin) {
                val cookie = customCookie ?: this@Ytmusic.cookie
                cookie?.let { cookie ->
                    append("Cookie", cookie)
                    if ("SAPISID" !in cookieMap || "__Secure-3PAPISID" !in cookieMap) return@let
                    val currentTime = now().toInstant(TimeZone.currentSystemDefault()).epochSeconds / 1000
                    val sapisidCookie = cookieMap["SAPISID"] ?: cookieMap["__Secure-3PAPISID"]
                    val sapisidHash = sha1("$currentTime $sapisidCookie https://music.youtube.com")
                    Logger.d(TAG, "SAPI SID Hash: SAPISIDHASH ${currentTime}_$sapisidHash")
                    append("Authorization", "SAPISIDHASH ${currentTime}_$sapisidHash")
                }
            }
        }
        userAgent(client.userAgent)
        parameter("prettyPrint", false)
    }

    @OptIn(ExperimentalTime::class)
    internal fun getAuthorizationHeader(): String? =
        cookie?.let { cookie ->
            if ("SAPISID" !in cookieMap || "__Secure-3PAPISID" !in cookieMap) null
            val currentTime = now().toInstant(TimeZone.currentSystemDefault()).epochSeconds / 1000
            val sapisidCookie = cookieMap["SAPISID"] ?: cookieMap["__Secure-3PAPISID"]
            val sapisidHash = sha1("$currentTime $sapisidCookie https://music.youtube.com")
            Logger.d(TAG, "SAPI SID Hash: SAPISIDHASH ${currentTime}_$sapisidHash")
            "SAPISIDHASH ${currentTime}_$sapisidHash"
        }

    fun getNewPipePlayer(videoId: String): List<Pair<Int, String>> = extractor.newPipePlayer(videoId)

    fun mergeAudioVideoDownload(filePath: String): DownloadProgress = extractor.mergeAudioVideoDownload(filePath)

    fun saveAudioWithThumbnail(
        filePath: String,
        track: SongItem,
    ) = extractor.saveAudioWithThumbnail(filePath, track)

    suspend fun search(
        client: YouTubeClient,
        query: String? = null,
        params: String? = null,
        continuation: String? = null,
    ) = httpClient.post("search") {
        ytClient(client, true)
        setBody(
            SearchBody(
                context = client.toContext(locale, visitorData),
                query = query,
                params = params,
            ),
        )
        parameter("continuation", continuation)
        parameter("ctoken", continuation)
    }

    suspend fun returnYouTubeDislike(videoId: String) =
        httpClient.get("https://returnyoutubedislikeapi.com/Votes?videoId=$videoId") {
            contentType(ContentType.Application.Json)
        }

    suspend fun ghostRequest(
        videoId: String,
        playlistId: String?,
    ) = httpClient
        .get(
            "https://www.youtube.com/watch?v=$videoId&bpctr=9999999999&has_verified=1"
                .let {
                    if (playlistId != null) "$it&list=$playlistId" else it
                },
        ) {
            headers {
                header("Connection", "close")
                header("Host", "www.youtube.com")
                header("Cookie", if (cookie.isNullOrEmpty()) "PREF=hl=en&tz=UTC; SOCS=CAI" else cookie)
                header("Sec-Fetch-Mode", "navigate")
                header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.71 Safari/537.36",
                )
            }
        }

    private fun HttpRequestBuilder.poHeader() {
        headers {
            header("accept", "*/*")
            header("origin", "https://www.youtube.com")
            header("content-type", "application/json+protobuf")
            header("priority", "u=1, i")
            header("referer", "https://www.youtube.com/")
            header("sec-ch-ua", "\"Microsoft Edge\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"")
            header("sec-ch-ua-mobile", "?0")
            header("sec-ch-ua-platform", "\"macOS\"")
            header("sec-fetch-dest", "empty")
            header("sec-fetch-mode", "cors")
            header("sec-fetch-site", "cross-site")
            header(
                "user-agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
            )
            header("x-goog-api-key", "AIzaSyDyT5W0Jh49F30Pqqtyfdf7pDLFKLJoAnw")
            header("x-user-agent", "grpc-web-javascript/0.1")
        }
    }

    suspend fun createPoTokenChallenge() =
        httpClient.post(
            "https://jnn-pa.googleapis.com/\$rpc/google.internal.waa.v1.Waa/Create",
        ) {
            poHeader()
            setBody("[\"$poTokenChallengeRequestKey\"]")
        }

    suspend fun generatePoToken(challenge: String) =
        httpClient.post(
            "https://jnn-pa.googleapis.com/\$rpc/google.internal.waa.v1.Waa/GenerateIT",
        ) {
            poHeader()
            setBody("[\"$poTokenChallengeRequestKey\", \"$challenge\"]")
        }

//    curl 'https://jnn-pa.googleapis.com/$rpc/google.internal.waa.v1.Waa/Create' \
//    -H 'accept: */*' \
//    -H 'accept-language: vi,en;q=0.9,en-GB;q=0.8,en-US;q=0.7' \
//    -H 'content-type: application/json+protobuf' \
//    -H 'origin: https://www.youtube.com' \
//    -H 'priority: u=1, i' \
//    -H 'referer: https://www.youtube.com/' \
//    -H 'sec-ch-ua: "Microsoft Edge";v="131", "Chromium";v="131", "Not_A Brand";v="24"' \
//    -H 'sec-ch-ua-mobile: ?0' \
//    -H 'sec-ch-ua-platform: "macOS"' \
//    -H 'sec-fetch-dest: empty' \
//    -H 'sec-fetch-mode: cors' \
//    -H 'sec-fetch-site: cross-site' \
//    -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0' \
//    -H 'x-goog-api-key: AIzaSyDyT5W0Jh49F30Pqqtyfdf7pDLFKLJoAnw' \
//    -H 'x-user-agent: grpc-web-javascript/0.1' \
//    --data-raw '["O43z0dpjhgX20SCx4KAo"]'

    suspend fun noLogInPlayer(
        videoId: String,
        cookie: String,
        visitorData: String?,
        poToken: String,
    ) = httpClient.post("https://www.youtube.com/youtubei/v1/player") {
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
        header("Host", "www.youtube.com")
        header("Origin", "https://www.youtube.com")
        header("Sec-Fetch-Mode", "navigate")
        header(HttpHeaders.UserAgent, IOS.userAgent)
        header(
            "Set-Cookie",
            cookie,
        )
        header("X-Goog-Visitor-Id", visitorData ?: this@Ytmusic.visitorData)
        header("X-YouTube-Client-Name", IOS.clientName)
        header("X-YouTube-Client-Version", IOS.clientVersion)
        setBody(
            PlayerBody(
                context = IOS.toContext(locale, null),
                playlistId = null,
                cpn = null,
                videoId = videoId,
                playbackContext = PlayerBody.PlaybackContext(),
                serviceIntegrityDimensions =
                    PlayerBody.ServiceIntegrityDimensions(
                        poToken = poToken,
                    ),
            ),
        )
        parameter("prettyPrint", false)
    }

    suspend fun getSimpMusicChart() =
        httpClient.get("https://chart.simpmusic.org/api/playlists") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }

    suspend fun test403Error(url: String): Boolean = httpClient.get(url).status.value in 200..299

    suspend fun player(
        client: YouTubeClient,
        videoId: String,
        playlistId: String?,
        cpn: String?,
        signatureTimestamp: Int? = null,
    ) = httpClient.post("player") {
        ytClient(client, setLogin = true)
        setBody(
            PlayerBody(
                context =
                    client.toContext(locale, visitorData).let {
                        if (client == TVHTML5) {
                            it.copy(
                                thirdParty =
                                    Context.ThirdParty(
                                        embedUrl = "https://www.youtube.com/watch?v=$videoId",
                                    ),
                            )
                        } else {
                            it
                        }
                    },
                videoId = videoId,
                playlistId = playlistId,
                cpn = cpn,
                playbackContext =
                    PlayerBody.PlaybackContext(
                        contentPlaybackContext =
                            PlayerBody.PlaybackContext.ContentPlaybackContext(
                                signatureTimestamp = signatureTimestamp ?: 20073,
                            ),
                    ),
//                serviceIntegrityDimensions =
//                    if (poToken != null) {
//                        PlayerBody.ServiceIntegrityDimensions(
//                            poToken = poToken,
//                        )
//                    } else {
//                        null
//                    },
            ),
        )
    }

    suspend fun pipedStreams(
        videoId: String,
        pipedInstance: String,
    ) = httpClient.get("$pipedInstance/streams/$videoId") {
        contentType(ContentType.Application.Json)
    }

    suspend fun getSuggestQuery(query: String) =
        httpClient.get("http://suggestqueries.google.com/complete/search") {
            contentType(ContentType.Application.Json)
            parameter("client", "firefox")
            parameter("ds", "yt")
            parameter("q", query)
        }

    suspend fun getYouTubeCaption(url: String) =
        httpClient.get(url) {
            contentType(ContentType.Text.Xml)
            headers {
                append(HttpHeaders.Accept, "text/xml; charset=UTF-8")
            }
        }

    suspend fun createYouTubePlaylist(
        title: String,
        listVideoId: List<String>?,
    ) = httpClient.post("playlist/create") {
        ytClient(WEB_REMIX, setLogin = true)
        setBody(
            CreatePlaylistBody(
                context = WEB_REMIX.toContext(locale, visitorData),
                title = title,
                videoIds = listVideoId,
            ),
        )
    }

    suspend fun editYouTubePlaylist(
        playlistId: String,
        title: String? = null,
    ) = httpClient.post("browse/edit_playlist") {
        ytClient(WEB_REMIX, setLogin = true)
        setBody(
            EditPlaylistBody(
                context = WEB_REMIX.toContext(locale, visitorData),
                playlistId = playlistId.removePrefix("VL"),
                actions =
                    listOf(
                        EditPlaylistBody.Action(
                            action = "ACTION_SET_PLAYLIST_NAME",
                            playlistName = title ?: "",
                        ),
                    ),
            ),
        )
    }

    suspend fun addItemYouTubePlaylist(
        playlistId: String,
        videoId: String,
    ) = httpClient.post("browse/edit_playlist") {
        ytClient(WEB_REMIX, setLogin = true)
        setBody(
            EditPlaylistBody(
                context = WEB_REMIX.toContext(locale, visitorData),
                playlistId = playlistId.removePrefix("VL"),
                actions =
                    listOf(
                        EditPlaylistBody.Action(
                            playlistName = null,
                            action = "ACTION_ADD_VIDEO",
                            addedVideoId = videoId,
                        ),
                    ),
            ),
        )
    }

    suspend fun removeItemYouTubePlaylist(
        playlistId: String,
        videoId: String,
        setVideoId: String,
    ) = httpClient.post("browse/edit_playlist") {
        ytClient(WEB_REMIX, setLogin = true)
        setBody(
            EditPlaylistBody(
                context = WEB_REMIX.toContext(locale, visitorData),
                playlistId = playlistId.removePrefix("VL"),
                actions =
                    listOf(
                        EditPlaylistBody.Action(
                            action = "ACTION_REMOVE_VIDEO",
                            removedVideoId = videoId,
                            setVideoId = setVideoId,
                        ),
                    ),
            ),
        )
    }

    /**
     * Move a playlist item before another item.
     * @param playlistId The YouTube playlist ID
     * @param setVideoId The setVideoId of the item to move
     * @param movedSetVideoIdSuccessor The setVideoId of the item that should come AFTER the moved item.
     *        If null, the item is moved to the end of the playlist.
     */
    suspend fun moveItemYouTubePlaylist(
        playlistId: String,
        setVideoId: String,
        movedSetVideoIdSuccessor: String? = null,
    ) = httpClient.post("browse/edit_playlist") {
        ytClient(WEB_REMIX, setLogin = true)
        setBody(
            EditPlaylistBody(
                context = WEB_REMIX.toContext(locale, visitorData),
                playlistId = playlistId.removePrefix("VL"),
                actions =
                    listOf(
                        EditPlaylistBody.Action(
                            action = "ACTION_MOVE_VIDEO_BEFORE",
                            setVideoId = setVideoId,
                            movedSetVideoIdSuccessor = movedSetVideoIdSuccessor,
                        ),
                    ),
            ),
        )
    }

    /***
     * SponsorBlock testing
     * @author maxrave-dev
     */

    suspend fun getSkipSegments(videoId: String) =
        httpClient.get("https://sponsor.ajay.app/api/skipSegments/") {
            contentType(ContentType.Application.Json)
            parameter("videoID", videoId)
            parameter("category", "sponsor")
            parameter("category", "selfpromo")
            parameter("category", "interaction")
            parameter("category", "intro")
            parameter("category", "outro")
            parameter("category", "preview")
            parameter("category", "music_offtopic")
            parameter("category", "poi_highlight")
            parameter("category", "filler")
            parameter("service", "YouTube")
        }

    suspend fun checkForGithubReleaseUpdate() =
        httpClient.get("https://api.github.com/repos/maxrave-dev/SimpMusic/releases/latest") {
            contentType(ContentType.Application.Json)
        }

    suspend fun checkForFdroidUpdate() =
        httpClient.get("https://f-droid.org/api/v1/packages/com.abcg.music") {
            contentType(ContentType.Application.Json)
        }

    suspend fun playlist(playlistId: String) =
        httpClient.post("browse") {
            ytClient(WEB_REMIX, !cookie.isNullOrEmpty())
            setBody(
                BrowseBody(
                    context =
                        WEB_REMIX.toContext(
                            locale,
                            visitorData,
                        ),
                    browseId = playlistId,
                    params = "wAEB",
                ),
            )
            parameter("alt", "json")
        }

    suspend fun browse(
        client: YouTubeClient,
        browseId: String? = null,
        params: String? = null,
        continuation: String? = null,
        countryCode: String? = null,
        setLogin: Boolean = false,
    ) = httpClient.post("browse") {
        ytClient(client, if (setLogin) true else cookie != "" && cookie != null, isUsingReferer = false)

        if (continuation != null && browseId != null) {
            setBody(
                BrowseBody(
                    context = client.toContext(locale, visitorData),
                    browseId = browseId.ifEmpty { null },
                    params = params,
                    continuation = continuation,
                ),
            )
        } else if (continuation != null) {
            setBody(
                BrowseBody(
                    context = client.toContext(locale, visitorData),
                    params = params,
                    continuation = continuation,
                ),
            )
        } else if (countryCode != null) {
            setBody(
                BrowseBody(
                    context = client.toContext(locale, visitorData),
                    browseId = if (browseId.isNullOrEmpty()) null else browseId,
                    params = params,
                    formData = FormData(listOf(countryCode)),
                ),
            )
        } else {
            setBody(
                BrowseBody(
                    context = client.toContext(locale, visitorData),
                    browseId = if (browseId.isNullOrEmpty()) null else browseId,
                    params = params,
                ),
            )
        }
    }

    suspend fun nextCustom(
        client: YouTubeClient,
        videoId: String,
    ) = httpClient.post("next") {
        ytClient(client, setLogin = false)
        setBody(
            BrowseBody(
                context = client.toContext(locale, visitorData),
                browseId = null,
                params = "wAEB",
                enablePersistentPlaylistPanel = true,
                isAudioOnly = true,
                tunerSettingValue = "AUTOMIX_SETTING_NORMAL",
                playlistId = "RDAMVM$videoId",
                watchEndpointMusicSupportedConfigs =
                    WatchEndpoint.WatchEndpointMusicSupportedConfigs(
                        WatchEndpoint.WatchEndpointMusicSupportedConfigs.WatchEndpointMusicConfig(
                            musicVideoType = "MUSIC_VIDEO_TYPE_ATV",
                        ),
                    ),
            ),
        )
        parameter("alt", "json")
    }

    suspend fun nextCtoken(
        client: YouTubeClient,
        continuation: String,
    ) = httpClient.post("browse") {
        ytClient(client, setLogin = true)
        parameter("ctoken", continuation)
        parameter("continuation", continuation)
        parameter("type", "next")
        parameter("prettyPrint", false)
        setBody(
            BrowseBody(
                context = client.toContext(locale, visitorData),
            ),
        )
    }

    suspend fun next(
        client: YouTubeClient,
        videoId: String?,
        playlistId: String?,
        playlistSetVideoId: String?,
        index: Int?,
        params: String?,
        continuation: String? = null,
    ) = httpClient.post("next") {
        ytClient(client, setLogin = true)
        setBody(
            NextBody(
                context = client.toContext(locale, visitorData),
                videoId = videoId,
                playlistId = playlistId,
                playlistSetVideoId = playlistSetVideoId,
                index = index,
                params = params,
                continuation = continuation,
            ),
        )
    }

    suspend fun getSearchSuggestions(
        client: YouTubeClient,
        input: String,
    ) = httpClient.post("music/get_search_suggestions") {
        ytClient(client)
        setBody(
            GetSearchSuggestionsBody(
                context = client.toContext(locale, visitorData),
                input = input,
            ),
        )
    }

    suspend fun getQueue(
        client: YouTubeClient,
        videoIds: List<String>?,
        playlistId: String?,
    ) = httpClient.post("music/get_queue") {
        ytClient(client)
        setBody(
            GetQueueBody(
                context = client.toContext(locale, visitorData),
                videoIds = videoIds,
                playlistId = playlistId,
            ),
        )
    }

    suspend fun getSwJsData() = httpClient.get("https://music.youtube.com/sw.js_data")

    suspend fun accountMenu(
        customCookie: String? = null,
        client: YouTubeClient,
    ) = httpClient.post("account/account_menu") {
        ytClient(client, setLogin = true, customCookie = customCookie)
        setBody(AccountMenuBody(client.toContext(locale, visitorData)))
    }

    suspend fun getAccountSwitcherEndpoint(customCookie: String? = null) =
        httpClient.get("https://music.youtube.com/getAccountSwitcherEndpoint") {
            ytClient(WEB_REMIX, setLogin = true, customCookie = customCookie)
        }

    suspend fun scrapeYouTube(videoId: String) =
        httpClient.get("https://www.youtube.com/watch?v=$videoId") {
            headers {
                append(HttpHeaders.AcceptLanguage, locale.hl)
                append(HttpHeaders.ContentLanguage, locale.gl)
            }
        }

    @OptIn(ExperimentalTime::class)
    suspend fun initPlayback(
        url: String,
        cpn: String,
        customParams: Map<String, String>? = null,
        playlistId: String?,
    ) = httpClient.get(url) {
        ytClient(WEB_REMIX, true)
        headers {
            append("X-Goog-Event-Time", now().toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds().toString())
            append("X-Goog-Request-Time", now().toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds().toString())
        }
        parameter("ver", "2")
        parameter("c", "WEB_REMIX")
        parameter("cpn", cpn)
        customParams?.forEach { (key, value) ->
            parameter(key, value)
        }
        if (playlistId != null) {
            parameter("list", playlistId)
            parameter("referrer", "https://music.youtube.com/playlist?list=$playlistId")
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun atr(
        url: String,
        cpn: String,
        customParams: Map<String, String>? = null,
        playlistId: String?,
    ) = httpClient.post(url) {
        ytClient(WEB_REMIX, true)
        headers {
            append("X-Goog-Event-Time", now().toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds().toString())
            append("X-Goog-Request-Time", now().toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds().toString())
        }
        parameter("cpn", cpn)
        customParams?.forEach { (key, value) ->
            parameter(key, value)
        }
        if (playlistId != null) {
            parameter("list", playlistId)
            parameter("referrer", "https://music.youtube.com/playlist?list=$playlistId")
        }
    }

    suspend fun addToLiked(videoId: String) =
        httpClient.post("like/like") {
            ytClient(WEB_REMIX, true)
            setBody(
                LikeBody(
                    context = WEB_REMIX.toContext(locale, visitorData),
                    target = LikeBody.Target(videoId),
                ),
            )
        }

    suspend fun removeFromLiked(videoId: String) =
        httpClient.post("like/removelike") {
            ytClient(WEB_REMIX, true)
            setBody(
                LikeBody(
                    context = WEB_REMIX.toContext(locale, visitorData),
                    target = LikeBody.Target(videoId),
                ),
            )
        }

    fun download(
        url: String,
        pathString: String,
        maxRetries: Int = 3,
        parallelDownloads: Int = DEFAULT_PARALLEL_DOWNLOADS,
    ): Flow<Triple<Boolean, Float, Int>> =
        channelFlow {
            val fileSystem = FileSystem.SYSTEM
            val path = pathString.toPath()
            val downloadBufferSize = 256 * 1024

            with(getDownloadClient()) {
                var lastException: Throwable? = null

                // First, check if server supports Range requests
                val supportsRange =
                    try {
                        val rangeResponse =
                            head(url) {
                                header("User-Agent", DOWNLOAD_USER_AGENT)
                                header("Range", "bytes=0-0")
                            }
                        rangeResponse.headers["Content-Range"] != null ||
                            rangeResponse.status.value in 200..299
                    } catch (e: Exception) {
                        false
                    }

                // Get file size
                val fileSize =
                    try {
                        head(url) {
                            header("User-Agent", DOWNLOAD_USER_AGENT)
                        }.headers[HttpHeaders.ContentLength]?.toLong() ?: 0L
                    } catch (e: Exception) {
                        0L
                    }

                // If server supports Range and file is large enough, use parallel download
                if (supportsRange && fileSize > CHUNK_SIZE * 2) {
                    parallelDownload(
                        url = url,
                        path = path,
                        fileSize = fileSize,
                        parallelDownloads = parallelDownloads,
                        downloadBufferSize = downloadBufferSize,
                        maxRetries = maxRetries,
                        onProgress = { downloaded, speed ->
                            trySend(Triple(false, downloaded, speed))
                        },
                        onComplete = { success, exception ->
                            lastException = exception
                            trySend(Triple(true, if (success) 1f else 0f, 0))
                        },
                    )
                } else {
                    // Fallback to single-threaded download
                    singleThreadedDownload(
                        url = url,
                        path = path,
                        maxRetries = maxRetries,
                        downloadBufferSize = downloadBufferSize,
                        onProgress = { downloaded, speed ->
                            trySend(Triple(false, downloaded, speed))
                        },
                        onComplete = { success, exception ->
                            lastException = exception
                            trySend(Triple(true, if (success) 1f else 0f, 0))
                        },
                    )
                }
            }
        }

    private suspend fun HttpClient.parallelDownload(
        url: String,
        path: Path,
        fileSize: Long,
        parallelDownloads: Int,
        downloadBufferSize: Int,
        maxRetries: Int,
        onProgress: suspend (Float, Int) -> Unit,
        onComplete: (Boolean, Throwable?) -> Unit,
    ) {
        val fileSystem = FileSystem.SYSTEM
        val chunkSize = fileSize / parallelDownloads
        val tempDir = path.parent?.let { it / "temp_chunks_${path.name}" } ?: throw IllegalArgumentException("Path has no parent")

        try {
            // Create temp directory
            fileSystem.createDirectories(tempDir)
            val tempFiles =
                (0 until parallelDownloads).map { index ->
                    tempDir / "chunk_$index.tmp"
                }

            // Download each chunk in parallel
            coroutineScope {
                val jobs =
                    (0 until parallelDownloads).map { index ->
                        val startByte = index * chunkSize
                        val endByte = if (index == parallelDownloads - 1) fileSize - 1L else (index + 1L) * chunkSize - 1L

                        launch {
                            var attempt = 0
                            var success = false

                            while (attempt <= maxRetries && !success) {
                                try {
                                    if (attempt > 0) {
                                        delay(500L * (1L shl (attempt - 1)).coerceAtMost(10000L))
                                    }

                                    prepareRequest {
                                        url(url)
                                        header("User-Agent", DOWNLOAD_USER_AGENT)
                                        header("Accept", "*/*")
                                        header("Range", "bytes=$startByte-$endByte")
                                    }.execute { res ->
                                        if (res.status.value != 206) {
                                            throw IllegalStateException("Server returned ${res.status.value} instead of 206 for Range request")
                                        }
                                        val channel = res.bodyAsChannel()
                                        fileSystem.sink(tempFiles[index]).buffer().use { sink ->
                                            while (!channel.isClosedForRead) {
                                                val packet = channel.readRemaining(downloadBufferSize.toLong())
                                                while (!packet.exhausted()) {
                                                    val bytes = packet.readByteArray()
                                                    sink.write(bytes)
                                                }
                                            }
                                        }
                                    }
                                    success = true
                                    Logger.d(TAG, "Chunk $index downloaded: $startByte-$endByte")
                                } catch (e: Exception) {
                                    Logger.e(TAG, "Chunk $index download failed: ${e.message}")
                                    attempt++
                                    if (attempt > maxRetries) {
                                        throw e
                                    }
                                }
                            }
                        }
                    }

                // Wait for all chunks and track progress
                var completedChunks = 0

                // Simplified progress tracking - emit after each chunk completes
                jobs.forEach { job ->
                    launch {
                        job.join()
                        completedChunks++
                        val downloaded = completedChunks * chunkSize
                        val progress = downloaded.toFloat() / fileSize.toFloat()
                        onProgress(progress, 0)
                    }
                }

                // Wait for all to complete
                jobs.forEach { it.join() }
            }

            // Merge all chunks into final file
            Logger.d(TAG, "Merging chunks into $path")
            fileSystem.sink(path).buffer().use { finalSink ->
                tempFiles.forEach { tempFile ->
                    if (fileSystem.exists(tempFile)) {
                        fileSystem.source(tempFile).use { source ->
                            finalSink.writeAll(source)
                        }
                        fileSystem.delete(tempFile)
                    }
                }
            }

            // Cleanup temp directory
            fileSystem.delete(tempDir)

            Logger.d(TAG, "Parallel download completed: $fileSize bytes")
            onComplete(true, null)
        } catch (e: Exception) {
            Logger.e(TAG, "Parallel download failed: ${e.message}")
            // Cleanup temp directory
            try {
                if (fileSystem.exists(tempDir)) {
                    fileSystem.deleteRecursively(tempDir)
                }
            } catch (ignored: Exception) {
                // Ignore cleanup errors
            }
            onComplete(false, e)
        }
    }

    private suspend fun HttpClient.singleThreadedDownload(
        url: String,
        path: Path,
        maxRetries: Int,
        downloadBufferSize: Int,
        onProgress: suspend (Float, Int) -> Unit,
        onComplete: (Boolean, Throwable?) -> Unit,
    ) {
        val fileSystem = FileSystem.SYSTEM
        var lastException: Throwable? = null
        var downloadedBytes = 0L
        var jobDone = 0

        repeat(maxRetries + 1) { attempt ->
            if (jobDone == 1) return@repeat

            if (attempt > 0) {
                try {
                    if (fileSystem.exists(path)) {
                        fileSystem.delete(path)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                downloadedBytes = 0L
                val delayMs = (500L * (1 shl (attempt - 1))).coerceAtMost(10000L)
                Logger.d(TAG, "Retry attempt $attempt after ${delayMs}ms delay")
                delay(delayMs)
            }

            try {
                val length =
                    head(url) {
                        header("User-Agent", DOWNLOAD_USER_AGENT)
                    }.headers[HttpHeaders.ContentLength]?.toLong() ?: 0

                coroutineScope {
                    val downloadJob =
                        launch {
                            runCatching {
                                prepareRequest {
                                    url(url)
                                    header("User-Agent", DOWNLOAD_USER_AGENT)
                                    header("Accept", "*/*")
                                }.execute { res ->
                                    val channel = res.bodyAsChannel()
                                    fileSystem.sink(path).buffer().use { sink ->
                                        while (!channel.isClosedForRead) {
                                            val packet = channel.readRemaining(downloadBufferSize.toLong())
                                            while (!packet.exhausted()) {
                                                val bytes = packet.readByteArray()
                                                sink.write(bytes)
                                                downloadedBytes += bytes.size
                                            }
                                        }
                                    }
                                }
                            }.onSuccess {
                                Logger.d(TAG, "Download completed: $downloadedBytes bytes")
                                jobDone = 1
                            }.onFailure { e ->
                                Logger.e(TAG, "Download failed: ${e.message}")
                                lastException = e
                                jobDone = 1
                            }
                        }

                    val emitJob =
                        launch {
                            var lastEmittedProgress = -1f
                            while (jobDone < 1) {
                                delay(200)
                                if (length > 0) {
                                    val progress = downloadedBytes.toFloat() / length
                                    val progressDiff =
                                        if (progress > lastEmittedProgress) {
                                            progress - lastEmittedProgress
                                        } else {
                                            lastEmittedProgress - progress
                                        }
                                    if (progressDiff >= 0.01f || progress >= 1f) {
                                        val elapsedSeconds = (downloadedBytes / 1024.0) / 200.0
                                        val speed = if (elapsedSeconds > 0) (downloadedBytes / elapsedSeconds / 1024).toInt() else 0
                                        lastEmittedProgress = progress
                                        onProgress(progress, speed)
                                    }
                                }
                            }
                        }

                    downloadJob.join()
                    emitJob.cancel()
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Download attempt $attempt failed: ${e.message}")
                lastException = e
                if (attempt == maxRetries) {
                    jobDone = 1
                }
            }
        }

        Logger.d(TAG, "Download finished: $downloadedBytes bytes")
        val isSuccess = lastException == null && downloadedBytes > 0
        onComplete(isSuccess, lastException)
    }

    suspend fun is403Url(url: String): Boolean {
        return try {
            return httpClient.head(url).status.value in 400..499
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    suspend fun searchTidalId(
        url: String,
        query: String,
    ) = httpClient.get("$url/search") {
        contentType(ContentType.Application.Json)
        header("accept", "*/*")
        parameter("s", query)
    }

    suspend fun getTidalStream(
        url: String,
        tidalId: String,
    ) = httpClient.get("$url/track") {
        contentType(ContentType.Application.Json)
        header("accept", "*/*")
        parameter("id", tidalId)
        parameter("quality", "HIGH")
    }

    suspend fun getTidalUptime() =
        httpClient.get(TIDAL_UPTIME_URL) {
            header("accept", "*/*")
            header("origin", "https://monochrome.tf")
        }

    companion object {
        const val TIDAL_UPTIME_URL = "https://tidal-uptime.jiffy-puffs-1j.workers.dev/"
    }
}

expect fun getCountry(): String

expect fun getLanguage(): String