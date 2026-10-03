package com.abcg.music.data.playlist

import com.abcg.music.data.generate.GeneratedTrack
import com.abcg.music.data.generate.youtubeVideoIdOrNull
import com.abcg.music.data.music.InnerTubeMusicApi
import com.abcg.music.data.music.YouTubeMusicTrack
import java.io.InputStream
import java.text.Normalizer
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

data class CsvRawTrack(
    val title: String,
    val artist: String,
    val album: String? = null,
    val videoId: String? = null,
)

data class CsvImportResult(
    val suggestedTitle: String,
    val totalRows: Int,
    val matchedCount: Int,
    val tracks: List<GeneratedTrack>,
)

@Singleton
class CsvPlaylistImporter @Inject constructor(
    private val innerTube: InnerTubeMusicApi,
) {
    suspend fun parseAndMatchCsv(
        inputStream: InputStream,
        filename: String = "Imported Playlist",
    ): CsvImportResult = withContext(Dispatchers.IO) {
        val bytes = inputStream.readBytes()
        val charset = when {
            bytes.take(2) == listOf(0xff.toByte(), 0xfe.toByte()) -> Charsets.UTF_16LE
            bytes.take(2) == listOf(0xfe.toByte(), 0xff.toByte()) -> Charsets.UTF_16BE
            else -> Charsets.UTF_8
        }
        val rawTracks = parseTracks(String(bytes, charset).removePrefix("\uFEFF"), filename)
        val limiter = Semaphore(4)
        val tracks = coroutineScope {
            rawTracks.map { raw ->
                async {
                    limiter.withPermit {
                        try {
                            val match = if (raw.videoId != null) {
                                innerTube.fetchSongDetails(raw.videoId)?.takeIf { it.videoId == raw.videoId }
                                    ?.takeIf { isExactMatch(raw, it) }
                            } else if (raw.title.isNotBlank() && raw.artist.isNotBlank()) {
                                val candidates = innerTube.searchSongs(
                                    query = "${raw.title} ${raw.artist}",
                                    limit = 30,
                                    prefetchStreams = false,
                                ).filter { isExactMatch(raw, it) }
                                candidates.singleOrNull()
                            } else null
                            match?.let {
                                GeneratedTrack(
                                    name = raw.title.ifBlank { it.title },
                                    artist = raw.artist.ifBlank { it.artist },
                                    album = raw.album ?: it.album,
                                    artworkUrl = it.artworkUrl,
                                    url = "https://music.youtube.com/watch?v=${it.videoId}",
                                )
                            }
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }
        CsvImportResult(
            suggestedTitle = filename.substringBeforeLast('.').replace(Regex("[_-]+"), " ").trim().ifBlank { "Imported Playlist" },
            totalRows = rawTracks.size,
            matchedCount = tracks.size,
            tracks = tracks,
        )
    }

    internal fun isExactMatch(source: CsvRawTrack, target: YouTubeMusicTrack): Boolean {
        if (!VIDEO_ID.matches(target.videoId)) return false
        if (source.videoId != null && source.videoId != target.videoId) return false
        if (source.videoId == null && (source.title.isBlank() || source.artist.isBlank())) return false
        if (normalize(source.artist) in setOf("unknown", "unknown artist")) return false
        if (source.title.isNotBlank() && !sameText(source.title, target.title, allowSafeVideoLabel = true)) return false
        if (source.artist.isNotBlank() && !sameArtist(source.artist, target.artist.removeSuffix(" - Topic"))) return false
        if (!source.album.isNullOrBlank() && !sameText(source.album, target.album.orEmpty())) return false
        return true
    }

    private fun sameText(source: String, target: String, allowSafeVideoLabel: Boolean = false): Boolean {
        val normalized = normalize(source)
        val targetText = if (allowSafeVideoLabel) stripSafeVideoLabel(target) else target
        return normalized.isNotBlank() && normalized == normalize(targetText)
    }

    private fun sameArtist(source: String, target: String): Boolean {
        if (sameText(source, target)) return true
        val primaryTarget = target
            .split(Regex("(?i)\\s*(?:,|&|feat\\.?|ft\\.?|featuring)\\s*"), limit = 2)
            .first()
            .removeSuffix(" - Topic")
            .trim()
        return sameText(source, primaryTarget)
    }

    private fun stripSafeVideoLabel(title: String): String = title
        .replace(
            Regex("(?i)\\s*[\\[(](official\\s*(audio|video)|music\\s*video|lyric\\s*video|audio|video|hd|hq|4k)[\\])]"),
            "",
        )
        .trim()

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFKC).lowercase(Locale.ROOT)
            .replace(Regex("['’]"), "")
            .replace(Regex("[^\\p{L}\\p{M}\\p{N}]+"), " ").trim()

    internal fun parseTracks(text: String, filename: String): List<CsvRawTrack> {
        val lines = text.lineSequence().map(String::trim).filter(String::isNotBlank).toList()
        if (lines.isEmpty()) return emptyList()
        if (filename.endsWith(".m3u", true) || filename.endsWith(".m3u8", true) || lines.first().equals("#EXTM3U", true)) {
            return parseM3u(text.lineSequence().map(String::trim).toList())
        }
        if (filename.endsWith(".txt", true) && lines.none { '\t' in it } &&
            lines.first().split(',', ';').map(::normalize).none { it in TITLE_HEADERS || it in ARTIST_HEADERS || it in URL_HEADERS }) {
            return lines.map(::parseTextTrack)
        }
        val delimiter = listOf(',', ';', '\t').maxBy { candidate ->
            parseRecords(text, candidate).take(5).sumOf { (it.size - 1).coerceAtLeast(0) }
        }
        val records = parseRecords(text, delimiter)
        val first = records.firstOrNull() ?: return emptyList()
        val headers = first.map(::normalize)
        val titleIndex = headers.indexOfFirst { it in TITLE_HEADERS }
        val artistIndex = headers.indexOfFirst { it in ARTIST_HEADERS }
        val albumIndex = headers.indexOfFirst { it in ALBUM_HEADERS }
        val urlIndex = headers.indexOfFirst { it in URL_HEADERS }
        val hasHeader = titleIndex >= 0 || artistIndex >= 0 || albumIndex >= 0 || urlIndex >= 0
        if (first.size == 1 && !hasHeader) return lines.filterNot { it.startsWith('#') }.map(::parseTextTrack)
        val titleColumn = if (hasHeader) titleIndex else 0
        val artistColumn = if (hasHeader) artistIndex else 1
        return records.drop(if (hasHeader) 1 else 0).mapNotNull { row ->
            if (hasHeader && row.map(::normalize) == headers) return@mapNotNull null
            val title = row.getOrNull(titleColumn).orEmpty().trim()
            val artist = row.getOrNull(artistColumn).orEmpty().trim()
            val videoId = youtubeId(row.getOrNull(urlIndex).orEmpty())
            if (title.isBlank() && videoId == null) return@mapNotNull null
            CsvRawTrack(title, artist, row.getOrNull(albumIndex)?.trim()?.takeIf(String::isNotBlank), videoId)
        }
    }

    private fun parseTextTrack(line: String): CsvRawTrack {
        if (line.startsWith("https://", true) || line.startsWith("http://", true)) {
            return CsvRawTrack("", "", videoId = youtubeId(line))
        }
        val separator = Regex("\\s+[-–—]\\s+").find(line)
            ?: return CsvRawTrack(line, "")
        return CsvRawTrack(
            title = line.substring(separator.range.last + 1).trim(),
            artist = line.substring(0, separator.range.first).trim(),
        )
    }

    private fun parseM3u(lines: List<String>): List<CsvRawTrack> {
        val tracks = mutableListOf<CsvRawTrack>()
        var pending: CsvRawTrack? = null
        for (line in lines) {
            when {
                line.startsWith("#EXTINF:", true) -> {
                    pending?.let { tracks += it }
                    val info = line.substringAfter(':')
                    val artist = Regex("""artist="([^"]+)""", RegexOption.IGNORE_CASE).find(info)?.groupValues?.get(1)
                    val title = Regex("""title="([^"]+)""", RegexOption.IGNORE_CASE).find(info)?.groupValues?.get(1)
                    pending = if (artist != null && title != null) CsvRawTrack(title, artist)
                    else parseTextTrack(info.substringAfter(',', ""))
                }
                line.isBlank() -> Unit
                line.startsWith('#') -> Unit
                else -> {
                    val videoId = youtubeId(line)
                    val track = pending ?: if (videoId != null) CsvRawTrack("", "", videoId = videoId)
                    else parseTextTrack(line.substringAfterLast('/').substringAfterLast('\\').substringBeforeLast('.'))
                    tracks += track.copy(videoId = videoId)
                    pending = null
                }
            }
        }
        pending?.let { tracks += it }
        return tracks
    }

    private fun youtubeId(value: String): String? {
        if (!VIDEO_ID.matches(value) && !value.startsWith("https://", true) && !value.startsWith("http://", true)) return null
        return GeneratedTrack(name = "", artist = "", artworkUrl = null, url = value).youtubeVideoIdOrNull()
    }

    private fun parseRecords(text: String, delimiter: Char): List<List<String>> {
        val records = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var index = 0
        fun finishField() {
            row += field.toString().trim()
            field.setLength(0)
        }
        fun finishRow() {
            finishField()
            if (row.any(String::isNotBlank)) records += row.toList()
            row.clear()
        }
        while (index < text.length) {
            val char = text[index]
            when {
                char == '"' && quoted && text.getOrNull(index + 1) == '"' -> {
                    field.append('"')
                    index++
                }
                char == '"' && (quoted || field.isBlank()) -> quoted = !quoted
                char == delimiter && !quoted -> finishField()
                (char == '\n' || char == '\r') && !quoted -> {
                    finishRow()
                    if (char == '\r' && text.getOrNull(index + 1) == '\n') index++
                }
                else -> field.append(char)
            }
            index++
        }
        require(!quoted) { "Playlist file contains an unclosed quoted field" }
        if (field.isNotEmpty() || row.isNotEmpty()) finishRow()
        return records
    }

    private companion object {
        val VIDEO_ID = Regex("[A-Za-z0-9_-]{11}")
        val TITLE_HEADERS = setOf("track name", "trackname", "title", "song", "name", "track", "song name", "songname", "song title", "track title")
        val ARTIST_HEADERS = setOf("artist name s", "artist names", "artist s", "artist", "artists", "artist name", "artistname", "track artist", "track artists", "performer", "author", "creator")
        val ALBUM_HEADERS = setOf("album name", "albumname", "album", "release")
        val URL_HEADERS = setOf("url", "uri", "track url", "track uri", "youtube url", "video id")
    }
}
