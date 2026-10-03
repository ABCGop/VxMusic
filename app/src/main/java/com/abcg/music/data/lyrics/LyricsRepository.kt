package com.abcg.music.data.lyrics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

fun isRtlText(text: CharSequence?): Boolean {
    if (text.isNullOrBlank()) return false
    var rtlCount = 0
    var ltrCount = 0
    var firstStrongRtl: Boolean? = null
    var i = 0
    while (i < text.length) {
        val codePoint = Character.codePointAt(text, i)
        when (Character.getDirectionality(codePoint)) {
            Character.DIRECTIONALITY_RIGHT_TO_LEFT,
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC -> {
                if (firstStrongRtl == null) firstStrongRtl = true
                rtlCount++
            }
            Character.DIRECTIONALITY_LEFT_TO_RIGHT -> {
                if (firstStrongRtl == null) firstStrongRtl = false
                ltrCount++
            }
        }
        i += Character.charCount(codePoint)
    }
    return (firstStrongRtl == true) || (rtlCount > 0 && rtlCount >= ltrCount)
}

data class LyricSyllable(
    val timeMs: Long,
    val durationMs: Long,
    val text: String,
    val isBackground: Boolean = false,
)

data class LyricLine(
    val timeMs: Long,
    val durationMs: Long = 0L,
    val text: String,
    val syllables: List<LyricSyllable> = emptyList(),
    val transliteration: String? = null,
    val transliterationSyllables: List<LyricSyllable> = emptyList(),
) {
    val hasSyllables: Boolean get() = syllables.isNotEmpty()
    val isRtl: Boolean get() = isRtlText(text) || syllables.any { isRtlText(it.text) }
}

sealed interface LyricsResult {
    data class Success(
        val lines: List<LyricLine>,
        val isSynced: Boolean,
        val isWordSynced: Boolean = false,
        val plainLyrics: String? = null,
        val isInstrumental: Boolean = false,
        val source: String? = null,
    ) : LyricsResult {
        val isRtl: Boolean get() = lines.any { it.isRtl } || isRtlText(plainLyrics)
    }

    data object Empty : LyricsResult
    data class Error(val message: String) : LyricsResult
}

@Singleton
class LyricsRepository @Inject constructor(
    private val lyricsPlusApi: LyricsPlusApi,
    private val betterLyricsApi: BetterLyricsApi,
    private val kugouApi: KugouLyricsApi,
    private val lrclibApi: LrclibLyricsApi,
    private val downloadedTrackDao: dagger.Lazy<com.abcg.music.data.local.db.DownloadedTrackDao>,
) {
    private val cache = ConcurrentHashMap<String, LyricsResult>()

    suspend fun getLyrics(
        title: String,
        artist: String,
        album: String? = null,
        durationSeconds: Int? = null,
        forceRefresh: Boolean = false,
        wordByWord: Boolean = true,
        onPartialResult: suspend (LyricsResult.Success) -> Unit = {},
    ): LyricsResult = withContext(Dispatchers.Default) {
        val cacheKey = "${artist.trim().lowercase()}|${title.trim().lowercase()}|${album?.trim()?.lowercase()}|$durationSeconds|$wordByWord"
        if (!forceRefresh) {
            cache[cacheKey]?.takeIf {
                !wordByWord || (it is LyricsResult.Success && (it.isWordSynced || it.isInstrumental))
            }?.let { return@withContext it }
        }

        var localLyrics: LyricsResult.Success? = null
        run {
            // 0. LOCAL OFFLINE: Check if this track is downloaded with embedded or saved lyrics
            val localTrack = runCatching {
                val dao = downloadedTrackDao.get()
                dao.findByTitleAndArtist(title, artist)
                    ?: dao.findByTrackKey("${artist.lowercase()}_${title.lowercase()}")
            }.getOrNull()
            if (localTrack != null && (localTrack.hasLyrics || !localTrack.plainLyrics.isNullOrBlank() || !localTrack.syncedLyrics.isNullOrBlank() || !localTrack.lrcFilePath.isNullOrBlank())) {
                var synced = localTrack.syncedLyrics
                if (synced.isNullOrBlank() && !localTrack.lrcFilePath.isNullOrBlank()) {
                    val lrcFile = java.io.File(localTrack.lrcFilePath)
                    if (lrcFile.exists() && lrcFile.length() > 0) {
                        synced = runCatching { lrcFile.readText() }.getOrNull()
                    }
                }
                if (!synced.isNullOrBlank()) {
                    val lines = parseLrc(synced)
                    if (lines.isNotEmpty()) {
                        val result = LyricsResult.Success(
                            lines = lines,
                            isSynced = true,
                            isWordSynced = false,
                            plainLyrics = localTrack.plainLyrics,
                            isInstrumental = false,
                            source = "Downloaded Lyrics (LRC)",
                        )
                        if (!wordByWord) {
                            cache[cacheKey] = result
                            return@withContext result
                        }
                        localLyrics = result
                        onPartialResult(result)
                        return@run
                    }
                }
                val plain = localTrack.plainLyrics
                if (!plain.isNullOrBlank()) {
                    val result = LyricsResult.Success(
                        lines = emptyList(),
                        isSynced = false,
                        isWordSynced = false,
                        plainLyrics = plain.trim(),
                        isInstrumental = false,
                        source = "Downloaded Lyrics (Plain)",
                    )
                    if (!wordByWord) {
                        cache[cacheKey] = result
                        return@withContext result
                    }
                    localLyrics = result
                    onPartialResult(result)
                }
            }
        }

        if (wordByWord) {
            val wordResult = coroutineScope {
                val requests = mutableListOf(
                    async<LyricsResult.Success?> {
                        try {
                            val wordResponse = lyricsPlusApi.fetchWordLyrics(title, artist, album, durationSeconds)
                            if (wordResponse != null && !wordResponse.lyrics.isNullOrEmpty()) {
                                val lines = wordResponse.lyrics.map { line ->
                                    val syllables = line.syllabus?.map { syl ->
                                        LyricSyllable(
                                            timeMs = syl.time,
                                            durationMs = syl.duration,
                                            text = syl.text,
                                            isBackground = syl.isBackground,
                                        )
                                    } ?: emptyList()

                                    val transliterationSyllables = line.transliteration?.syllabus?.map { syl ->
                                        LyricSyllable(
                                            timeMs = syl.time,
                                            durationMs = syl.duration,
                                            text = syl.text,
                                            isBackground = syl.isBackground,
                                        )
                                    } ?: emptyList()

                                    LyricLine(
                                        timeMs = line.time,
                                        durationMs = line.duration,
                                        text = line.text,
                                        syllables = syllables,
                                        transliteration = line.transliteration?.text,
                                        transliterationSyllables = transliterationSyllables,
                                    )
                                }.sortedBy { it.timeMs }

                                if (lines.isNotEmpty()) {
                                    val hasWordTiming = lines.any { it.hasSyllables }
                                    val result = LyricsResult.Success(
                                        lines = lines,
                                        isSynced = true,
                                        isWordSynced = hasWordTiming,
                                        plainLyrics = lines.joinToString("\n") { it.text },
                                        isInstrumental = false,
                                        source = if (hasWordTiming) "LyricsPlus (Word-Sync)" else "LyricsPlus (Line-Sync)",
                                    )
                                    return@async result
                                }
                            }
                        } catch (cancellation: kotlinx.coroutines.CancellationException) {
                            throw cancellation
                        } catch (_: Exception) {
                        }
                        null
                    },
                    async<LyricsResult.Success?> {
                        try {
                            val betterLines = betterLyricsApi.fetchWordLyrics(title, artist)
                            if (!betterLines.isNullOrEmpty()) {
                                val hasWordTiming = betterLines.any { it.hasSyllables }
                                val result = LyricsResult.Success(
                                    lines = betterLines,
                                    isSynced = true,
                                    isWordSynced = hasWordTiming,
                                    plainLyrics = betterLines.joinToString("\n") { it.text },
                                    isInstrumental = false,
                                    source = if (hasWordTiming) "BetterLyrics (Word-Sync)" else "BetterLyrics (Line-Sync)",
                                )
                                return@async result
                            }
                        } catch (cancellation: kotlinx.coroutines.CancellationException) {
                            throw cancellation
                        } catch (_: Exception) {
                        }
                        null
                    },
                    async<LyricsResult.Success?> {
                        try {
                            val kugouLines = kugouApi.fetchWordLyrics(title, artist, durationSeconds)
                            if (!kugouLines.isNullOrEmpty()) {
                                val hasWordTiming = kugouLines.any { it.hasSyllables }
                                val result = LyricsResult.Success(
                                    lines = kugouLines,
                                    isSynced = true,
                                    isWordSynced = hasWordTiming,
                                    plainLyrics = kugouLines.joinToString("\n") { it.text },
                                    isInstrumental = false,
                                    source = "Kugou KRC (Word-Sync)",
                                )
                                return@async result
                            }
                        } catch (cancellation: kotlinx.coroutines.CancellationException) {
                            throw cancellation
                        } catch (_: Exception) {
                        }
                        null
                    },
                )
                var lineFallback: LyricsResult.Success? = null
                try {
                    while (requests.isNotEmpty()) {
                        val (request, result) = select {
                            requests.forEach { request ->
                                request.onAwait { request to it }
                            }
                        }
                        requests.remove(request)
                        if (result?.isWordSynced == true) return@coroutineScope result
                        if (result != null && lineFallback == null) {
                            lineFallback = result
                            onPartialResult(result)
                        }
                    }
                    lineFallback
                } finally {
                    requests.forEach { it.cancel() }
                }
            }
            if (wordResult != null) {
                cache[cacheKey] = wordResult
                return@withContext wordResult
            }
        }
        localLyrics?.let { return@withContext it }

        // Fall back to LRCLIB line-by-line sync.
        val lrclibRecord = try {
            lrclibApi.fetchLyrics(title, artist, album, durationSeconds)
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            null
        }

        if (lrclibRecord != null) {
            if (lrclibRecord.instrumental == true) {
                val result = LyricsResult.Success(
                    lines = emptyList(),
                    isSynced = false,
                    isWordSynced = false,
                    plainLyrics = null,
                    isInstrumental = true,
                    source = "LRCLIB (Instrumental)",
                )
                cache[cacheKey] = result
                return@withContext result
            }

            val synced = lrclibRecord.syncedLyrics
            if (!synced.isNullOrBlank()) {
                val lines = parseLrc(synced)
                if (lines.isNotEmpty()) {
                    val result = LyricsResult.Success(
                        lines = lines,
                        isSynced = true,
                        isWordSynced = false,
                        plainLyrics = lrclibRecord.plainLyrics,
                        isInstrumental = false,
                        source = "LRCLIB (Line-Sync)",
                    )
                    cache[cacheKey] = result
                    return@withContext result
                }
            }

            val plain = lrclibRecord.plainLyrics
            if (!plain.isNullOrBlank()) {
                val result = LyricsResult.Success(
                    lines = emptyList(),
                    isSynced = false,
                    isWordSynced = false,
                    plainLyrics = plain.trim(),
                    isInstrumental = false,
                    source = "LRCLIB (Plain)",
                )
                cache[cacheKey] = result
                return@withContext result
            }
        }

        // 4. FALLBACK: If all fail, return Empty (no lyrics)
        LyricsResult.Empty
    }

    companion object {
        private val TIMESTAMP_REGEX = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{2,3}))?\]""")
        private val OFFSET_REGEX = Regex("""\[offset:\s*([+-]?\d+)\s*\]""", RegexOption.IGNORE_CASE)

        fun parseLrc(lrcContent: String): List<LyricLine> {
            val result = mutableListOf<LyricLine>()
            val lines = lrcContent.lines()
            var offsetMs = 0L

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue

                val offsetMatch = OFFSET_REGEX.find(trimmed)
                if (offsetMatch != null) {
                    offsetMs = offsetMatch.groupValues[1].toLongOrNull() ?: 0L
                    continue
                }

                // Check if line contains timestamp(s)
                val matches = TIMESTAMP_REGEX.findAll(trimmed).toList()
                if (matches.isEmpty()) continue

                // Extract text after all timestamps
                val text = trimmed.replace(TIMESTAMP_REGEX, "").trim()

                for (match in matches) {
                    val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                    val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                    val fractionStr = match.groupValues.getOrNull(3).orEmpty()
                    val fractionMs = when (fractionStr.length) {
                        2 -> (fractionStr.toLongOrNull() ?: 0L) * 10
                        3 -> fractionStr.toLongOrNull() ?: 0L
                        1 -> (fractionStr.toLongOrNull() ?: 0L) * 100
                        else -> 0L
                    }

                    val totalMs = (minutes * 60 * 1000) + (seconds * 1000) + fractionMs + offsetMs
                    result.add(
                        LyricLine(
                            timeMs = totalMs.coerceAtLeast(0L),
                            text = text,
                            syllables = emptyList(),
                        ),
                    )
                }
            }

            return result.sortedBy { it.timeMs }
        }
    }
}
