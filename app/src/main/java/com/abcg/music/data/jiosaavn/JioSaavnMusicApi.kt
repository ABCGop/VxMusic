package com.abcg.music.data.jiosaavn

import android.util.Log
import com.abcg.music.data.artwork.awaitSuccessfulBodyOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.Normalizer
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class JioSaavnSearchResponse(
    val total: Int = 0,
    val results: List<JioSaavnTrackItem> = emptyList(),
)

@Serializable
private data class JioSaavnTrackItem(
    val id: String = "",
    val song: String = "",
    val album: String = "",
    val year: String = "",
    @SerialName("primary_artists")
    val primaryArtists: String = "",
    @SerialName("featured_artists")
    val featuredArtists: String = "",
    val image: String = "",
    val duration: String = "0",
    @SerialName("encrypted_media_url")
    val encryptedMediaUrl: String = "",
    @SerialName("perma_url")
    val permaUrl: String = "",
)

@Singleton
class JioSaavnMusicApi @Inject constructor(
    okHttpClient: OkHttpClient,
) {
    private val client = okHttpClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val TAG = "JioSaavnMusicApi"
        private const val BASE_URL = "https://www.jiosaavn.com/api.php"
        private const val DES_KEY = "38346591"
        private const val MAX_DURATION_DIFFERENCE_SECONDS = 15

        private val DIACRITICS = Regex("\\p{M}+")
        private val NON_ALPHANUMERIC = Regex("[^a-z0-9]+")
        private val MULTI_SPACE = Regex("\\s+")
        private val TOPIC_CHANNEL_SUFFIX = Regex("""(?i)\s*[-–—]\s*topic\s*$|\s+topic\s*$""")
        private val PIPE_NOISE = Regex("""\s*\|.*$""")
        private val FEATURING_CLAUSE = Regex("""(?i)(?:\s*[\[(])?\s*(feat\.?|ft\.?|featuring)\s+.*$""")
        private val BRACKETED_DISPLAY_NOISE = Regex(
            """(?i)[\[(]\s*(?:explicit|clean|(?:official\s+)?(?:music\s+)?(?:audio|video|lyrics?|lyric\s+video|visualizer|hd|4k|mv|full\s+song|full\s+audio|prod\.?\s*(?:by\s*)?[^\])]+))\s*[\])]"""
        )
        private val TRAILING_DISPLAY_NOISE = Regex("""(?i)\s*[-–—]\s*(?:official\s+)?(?:music\s+)?(?:audio|video|lyrics?|visualizer|mv|full\s+song)\s*$""")
        private val ARTIST_NOISE_WORDS = setOf("the", "and", "feat", "ft", "featuring", "with", "x", "topic")
    }

    /**
     * Resolves a verified, high-quality 320 kbps AAC stream from JioSaavn CDN.
     */
    suspend fun resolveStream(
        title: String,
        artist: String,
        expectedDurationSeconds: Int? = null,
        expectedAlbum: String? = null,
        preferredBitrate: Int = 320,
    ): JioSaavnAudioStream? = withContext(Dispatchers.IO) {
        if (title.isBlank()) return@withContext null

        try {
            val candidate = findBestVerifiedMatch(
                title = title,
                artist = artist,
                expectedDurationSeconds = expectedDurationSeconds,
                expectedAlbum = expectedAlbum,
            ) ?: return@withContext null

            val decryptedUrl = decryptMediaUrl(candidate.encryptedMediaUrl) ?: return@withContext null
            val formattedUrl = formatAudioUrl(decryptedUrl, preferredBitrate)
            val durationSec = candidate.duration.toIntOrNull() ?: expectedDurationSeconds ?: 0

            val artwork = candidate.image.takeIf(String::isNotBlank)?.let { img ->
                img.replace("150x150", "500x500").replace("50x50", "500x500").replace("http://", "https://")
            }

            JioSaavnAudioStream(
                url = formattedUrl,
                mimeType = "audio/mp4",
                bitrateKbps = preferredBitrate,
                durationSeconds = durationSec,
                songId = candidate.id,
                title = unescapeHtml(candidate.song),
                artist = unescapeHtml(candidate.primaryArtists.ifBlank { candidate.featuredArtists }),
                album = unescapeHtml(candidate.album),
                artworkUrl = artwork,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            runCatching { Log.d(TAG, "JioSaavn stream resolution failed gracefully: ${e.message}") }
            null
        }
    }

    private suspend fun findBestVerifiedMatch(
        title: String,
        artist: String,
        expectedDurationSeconds: Int?,
        expectedAlbum: String?,
    ): JioSaavnTrackItem? {
        val cleanTitle = cleanForSearch(title)
        val cleanArtist = cleanForSearch(artist).takeIf { !it.equals("Unknown artist", ignoreCase = true) }.orEmpty()

        val individualArtists = cleanArtist.split(Regex("""(?i)\s*(?:&|,|\bx\b|feat\.?|ft\.?|featuring|with|\+)\s*"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val queries = listOfNotNull(
            "$cleanTitle $cleanArtist".trim().takeIf { it.isNotBlank() },
            individualArtists.firstOrNull()?.let { "$cleanTitle $it".trim() }?.takeIf { it.isNotBlank() && it != "$cleanTitle $cleanArtist" },
            cleanTitle.takeIf { it.isNotBlank() },
            title.trim().takeIf { it.isNotBlank() },
        ).distinct()

        for (query in queries) {
            currentCoroutineContext().ensureActive()
            val urlBuilder = BASE_URL.toHttpUrlOrNull()?.newBuilder() ?: continue
            urlBuilder.addQueryParameter("__call", "search.getResults")
            urlBuilder.addQueryParameter("_format", "json")
            urlBuilder.addQueryParameter("_marker", "0")
            urlBuilder.addQueryParameter("ctx", "wap6dot0")
            urlBuilder.addQueryParameter("p", "0")
            urlBuilder.addQueryParameter("n", "10")
            urlBuilder.addQueryParameter("q", query)

            val request = Request.Builder()
                .url(urlBuilder.build())
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .get()
                .build()

            val items = try {
                val body = client.newCall(request).awaitSuccessfulBodyOrNull() ?: continue
                json.decodeFromString<JioSaavnSearchResponse>(body).results
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                runCatching { Log.d(TAG, "JioSaavn search query '$query' failed: ${e.message}") }
                emptyList()
            }

            if (items.isEmpty()) continue

            val bestMatch = items.mapNotNull { item ->
                if (item.encryptedMediaUrl.isBlank()) return@mapNotNull null
                val score = verifiedMatchScore(
                    item = item,
                    title = title,
                    artist = artist,
                    expectedDurationSeconds = expectedDurationSeconds,
                    expectedAlbum = expectedAlbum,
                ) ?: return@mapNotNull null
                item to score
            }.maxByOrNull { it.second }?.first

            if (bestMatch != null) return bestMatch
        }

        return null
    }

    private fun verifiedMatchScore(
        item: JioSaavnTrackItem,
        title: String,
        artist: String,
        expectedDurationSeconds: Int?,
        expectedAlbum: String?,
    ): Int? {
        val rawItemTitle = unescapeHtml(item.song)
        val rawItemArtist = unescapeHtml(item.primaryArtists)
        val matchArtist = cleanForSearch(artist).ifBlank { artist }
        val targetTitle = normalizeTitle(title, matchArtist)
        val candidateTitle = normalizeTitle(rawItemTitle, matchArtist)
        if (targetTitle.isBlank() || candidateTitle.isBlank()) return null

        val targetArtists = matchArtist.split(Regex("""(?i)\s*(?:&|,|\bx\b|feat\.?|ft\.?|featuring|with|\+)\s*"""))
            .map(::normalizeText)
            .filter(String::isNotBlank)

        val candidateArtists = rawItemArtist.split(Regex("""(?i)\s*(?:&|,|\bx\b|feat\.?|ft\.?|featuring|with|\+)\s*"""))
            .map(::normalizeText)
            .filter(String::isNotBlank)

        val artistMatch = targetArtists.isEmpty() || candidateArtists.isEmpty() ||
            targetArtists.any { ta -> candidateArtists.any { ca -> ta == ca || ca.contains(ta) || ta.contains(ca) } }

        val titleDistance = levenshtein(targetTitle, candidateTitle)
        val isExactMatch = targetTitle == candidateTitle
        val maxFuzz = (targetTitle.length / 4).coerceIn(1, 4)
        val isFuzzyMatch = (artistMatch || targetArtists.isEmpty()) && titleDistance <= maxFuzz
        val isSubstringMatch = (targetTitle.contains(candidateTitle) || candidateTitle.contains(targetTitle)) &&
            (artistMatch || targetTitle.length > 5)

        if (!isExactMatch && !isFuzzyMatch && !isSubstringMatch) return null

        val candidateDuration = item.duration.toIntOrNull() ?: 0
        val durationDifference = if (expectedDurationSeconds != null && expectedDurationSeconds > 0 && candidateDuration > 0) {
            kotlin.math.abs(candidateDuration - expectedDurationSeconds).also {
                if (it > MAX_DURATION_DIFFERENCE_SECONDS) return null
            }
        } else null

        var score = 1_000 - titleDistance * 50
        if (artistMatch) score += 300
        if (isExactMatch) score += 200
        durationDifference?.let { score += (MAX_DURATION_DIFFERENCE_SECONDS - it) * 10 }

        expectedAlbum?.takeIf(String::isNotBlank)?.let { album ->
            if (normalizeText(album) == normalizeText(unescapeHtml(item.album))) score += 100
        }

        return score
    }

    /**
     * Decrypts JioSaavn's DES-encrypted media URL.
     */
    fun decryptMediaUrl(encryptedUrl: String): String? {
        if (encryptedUrl.isBlank()) return null
        val cleaned = encryptedUrl.trim().replace("\\/", "/")
        return try {
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            val keySpec = SecretKeySpec(DES_KEY.toByteArray(Charsets.UTF_8), "DES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decoded = try {
                java.util.Base64.getDecoder().decode(cleaned)
            } catch (_: Throwable) {
                try {
                    android.util.Base64.decode(cleaned, android.util.Base64.DEFAULT)
                } catch (_: Throwable) {
                    null
                }
            } ?: return null
            val decryptedBytes = cipher.doFinal(decoded)
            String(decryptedBytes, Charsets.UTF_8).trim()
        } catch (e: Exception) {
            runCatching { Log.d(TAG, "DES decryption failed: ${e.message}") }
            null
        }
    }

    /**
     * Formats raw decrypted JioSaavn CDN URL to the target bitrate (default 320 kbps).
     */
    fun formatAudioUrl(rawUrl: String, preferredBitrate: Int = 320): String {
        val targetSuffix = when (preferredBitrate) {
            12 -> "_12.mp4"
            48 -> "_48.mp4"
            96 -> "_96.mp4"
            160 -> "_160.mp4"
            else -> "_320.mp4"
        }
        return rawUrl
            .replace(Regex("""_96(?:_p)?\.mp4"""), targetSuffix)
            .replace(Regex("""_48(?:_p)?\.mp4"""), targetSuffix)
            .replace(Regex("""_160(?:_p)?\.mp4"""), targetSuffix)
            .replace(Regex("""_320(?:_p)?\.mp4"""), targetSuffix)
            .replace("http://", "https://")
    }

    private fun cleanForSearch(raw: String): String {
        return raw
            .replace(TOPIC_CHANNEL_SUFFIX, "")
            .replace(PIPE_NOISE, "")
            .replace(FEATURING_CLAUSE, " ")
            .replace(BRACKETED_DISPLAY_NOISE, " ")
            .replace(TRAILING_DISPLAY_NOISE, " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun normalizeTitle(raw: String, artist: String): String {
        var cleaned = cleanForSearch(raw)
        if (artist.isNotBlank()) {
            val cleanArt = cleanForSearch(artist).ifBlank { artist }
            cleaned = cleaned.replaceFirst(
                Regex("""^\s*${Regex.escape(cleanArt)}\s*[-–—:]\s*""", RegexOption.IGNORE_CASE),
                "",
            )
        }
        return normalizeText(cleaned)
    }

    private fun normalizeText(text: String): String {
        val normalizedDiacritics = Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        val withoutDiacritics = DIACRITICS.replace(normalizedDiacritics, "")
        return NON_ALPHANUMERIC.replace(withoutDiacritics, " ")
            .replace(MULTI_SPACE, " ")
            .trim()
    }

    private fun unescapeHtml(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&#039;", "'")
            .replace("&quot;", "\"")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }

    private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        if (lhs == rhs) return 0
        if (lhs.isEmpty()) return rhs.length
        if (rhs.isEmpty()) return lhs.length

        val v0 = IntArray(rhs.length + 1) { it }
        val v1 = IntArray(rhs.length + 1)

        for (i in lhs.indices) {
            v1[0] = i + 1
            for (j in rhs.indices) {
                val cost = if (lhs[i] == rhs[j]) 0 else 1
                v1[j + 1] = minOf(v1[j] + 1, v0[j + 1] + 1, v0[j] + cost)
            }
            System.arraycopy(v1, 0, v0, 0, v0.size)
        }
        return v0[rhs.length]
    }
}
