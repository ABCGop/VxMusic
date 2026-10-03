package com.abcg.music.data.lyrics

import com.abcg.music.data.artwork.awaitSuccessfulBodyOrNull
import kotlinx.coroutines.CancellationException

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class BetterLyricsGetResponse(
    val ttml: String? = null,
)

@Serializable
private data class BetterLyricsTtmlResponse(
    val lyrics: String? = null,
)

/**
 * BetterLyrics word-sync provider (https://lyrics-api.boidu.dev).
 *
 * Free, no API key, GPL-3.0. Serves Apple-Music TTML with per-syllable
 * `<span begin end>` timing, which maps 1:1 onto [LyricLine]/[LyricSyllable].
 *
 * Chain position: after LyricsPlus, before Kugou — both TTML endpoints are
 * tried (`/getLyrics` returns `{"ttml"}`, `/ttml/getLyrics` returns
 * `{"lyrics"}`).
 */
@Singleton
class BetterLyricsApi @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun fetchWordLyrics(
        title: String,
        artist: String,
    ): List<LyricLine>? = withContext(Dispatchers.IO) {
        if (title.isBlank() || artist.isBlank()) return@withContext null

        queryEndpoints(title, artist)?.let { return@withContext it }

        val cleanedTitle = LrclibLyricsApi.cleanTrackTitle(title)
        val cleanedArtist = LrclibLyricsApi.cleanArtistName(artist)
        if (cleanedTitle != title || cleanedArtist != artist) {
            queryEndpoints(cleanedTitle, cleanedArtist)?.let { return@withContext it }
        }

        null
    }

    private suspend fun queryEndpoints(title: String, artist: String): List<LyricLine>? {
        // Primary: {"ttml": "<tt ...>"} — params are s (song) + a (artist).
        fetchTtml(
            baseUrl = "https://lyrics-api.boidu.dev/getLyrics",
            title = title,
            artist = artist,
            field = BetterField.TTML,
        )?.let { return it }

        // Fallback: {"lyrics": "<tt ...>"}.
        fetchTtml(
            baseUrl = "https://lyrics-api.boidu.dev/ttml/getLyrics",
            title = title,
            artist = artist,
            field = BetterField.LYRICS,
        )?.let { return it }

        return null
    }

    private enum class BetterField { TTML, LYRICS }

    private suspend fun fetchTtml(
        baseUrl: String,
        title: String,
        artist: String,
        field: BetterField,
    ): List<LyricLine>? {
        val url = baseUrl.toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("s", title.trim())
            ?.addQueryParameter("a", artist.trim())
            ?.build() ?: return null

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "LastWave-Android/1.0 (https://github.com/duxtami/LastWave)")
            .header("Accept", "application/json")
            .get()
            .build()

        return try {
            val body = okHttpClient.newCall(request).awaitSuccessfulBodyOrNull() ?: return null
            val ttml = when (field) {
                BetterField.TTML -> json.decodeFromString<BetterLyricsGetResponse>(body).ttml
                BetterField.LYRICS -> json.decodeFromString<BetterLyricsTtmlResponse>(body).lyrics
            }
            if (ttml.isNullOrBlank()) return null
            parseTtml(ttml).takeIf { it.isNotEmpty() }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private val P_TAG_REGEX = Regex(
            """<p\b[^>]*\bbegin="([^"]+)"[^>]*\bend="([^"]+)"[^>]*>(.*?)</p>""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
        )
        private val SPAN_TAG_REGEX = Regex(
            """<span\b[^>]*\bbegin="([^"]+)"[^>]*\bend="([^"]+)"[^>]*>(.*?)</span>""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
        )
        private val XML_TAG_REGEX = Regex("""<[^>]+>""")

        fun parseTtml(ttml: String): List<LyricLine> {
            val lines = mutableListOf<LyricLine>()
            for (pMatch in P_TAG_REGEX.findAll(ttml)) {
                val lineStartMs = parseTtmlTime(pMatch.groupValues[1]) ?: continue
                val lineEndMs = parseTtmlTime(pMatch.groupValues[2]) ?: (lineStartMs + 1500L)
                val inner = pMatch.groupValues[3]

                val syllables = mutableListOf<LyricSyllable>()
                val words = mutableListOf<String>()
                for (sMatch in SPAN_TAG_REGEX.findAll(inner)) {
                    val wStart = parseTtmlTime(sMatch.groupValues[1]) ?: continue
                    val wEnd = parseTtmlTime(sMatch.groupValues[2]) ?: wStart
                    val word = unescapeXml(sMatch.groupValues[3].trim())
                    if (word.isEmpty()) continue
                    words.add(word)
                    syllables.add(
                        LyricSyllable(
                            timeMs = wStart,
                            durationMs = (wEnd - wStart).coerceAtLeast(0L),
                            text = word,
                        ),
                    )
                }

                // Lines without word spans (e.g. instrumental markers) still
                // carry line timing — keep them as line-sync lines.
                if (syllables.isEmpty()) {
                    val text = unescapeXml(inner.replace(XML_TAG_REGEX, "").trim())
                    if (text.isEmpty()) continue
                    lines.add(
                        LyricLine(
                            timeMs = lineStartMs,
                            durationMs = (lineEndMs - lineStartMs).coerceAtLeast(0L),
                            text = text,
                        ),
                    )
                } else {
                    lines.add(
                        LyricLine(
                            timeMs = lineStartMs,
                            durationMs = (lineEndMs - lineStartMs).coerceAtLeast(0L),
                            text = words.joinToString(" "),
                            syllables = syllables,
                        ),
                    )
                }
            }
            return lines.sortedBy { it.timeMs }
        }

        /** TTML times are seconds floats ("9.731") or clock times ("3:53.713"). */
        fun parseTtmlTime(raw: String): Long? {
            val value = raw.trim()
            if (value.isEmpty()) return null
            return try {
                if (':' in value) {
                    val parts = value.split(':')
                    var totalMs = 0L
                    for (i in parts.indices) {
                        val part = parts[i].toDoubleOrNull() ?: return null
                        val power = parts.size - 1 - i
                        totalMs += (part * 60.0.pow(power) * 1000L).toLong()
                    }
                    totalMs
                } else {
                    (value.toDoubleOrNull() ?: return null).times(1000L).toLong()
                }
            } catch (_: Exception) {
                null
            }
        }

        private fun Double.pow(n: Int): Double {
            var result = 1.0
            repeat(n) { result *= this }
            return result
        }

        fun unescapeXml(raw: String): String =
            raw.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&#39;", "'")
    }
}
