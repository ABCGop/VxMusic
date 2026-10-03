package com.abcg.music.data.playlist

import com.abcg.music.data.music.InnerTubeMusicApi
import com.abcg.music.data.music.YouTubeMusicTrack
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvPlaylistImporterTest {
    private val api = mockk<InnerTubeMusicApi>()
    private val importer = CsvPlaylistImporter(api)

    @Test
    fun rejectsDifferentArtistsVersionsAndMissingMetadata() {
        val source = CsvRawTrack("Song", "Artist")
        val exact = YouTubeMusicTrack("abcdefghijk", "Song", "Artist")
        assertTrue(importer.isExactMatch(source, exact))
        listOf("Song Live", "Song (Remastered)", "Song Remix", "Song Part 2").forEach {
            assertFalse(importer.isExactMatch(source, exact.copy(title = it)))
        }
        assertFalse(importer.isExactMatch(source, exact.copy(artist = "Artist Tribute")))
        assertFalse(importer.isExactMatch(source.copy(artist = ""), exact))
        assertFalse(importer.isExactMatch(source.copy(album = "Original"), exact.copy(album = "Compilation")))
        assertFalse(importer.isExactMatch(source.copy(title = "Song (Live)"), exact))
    }

    @Test
    fun preservesNonLatinIdentity() {
        val source = CsvRawTrack("तुम ही हो", "अरिजीत सिंह")
        val target = YouTubeMusicTrack("abcdefghijk", source.title, source.artist)
        assertTrue(importer.isExactMatch(source, target))
        assertFalse(importer.isExactMatch(source, target.copy(title = "केसरिया")))
    }

    @Test
    fun readsQuotedMultilineCsvWithoutLosingQuotes() {
        val rows = importer.parseTracks("Track,Artist\n\"Song, \"\"One\"\"\nLive\",Artist", "songs.csv")
        assertEquals(listOf(CsvRawTrack("Song, \"One\"\nLive", "Artist")), rows)
    }

    @Test
    fun readsSpotifyAndTabSeparatedHeadersWithoutGuessingMissingColumns() {
        val csv = "Track URI;Track Name;Artist URI(s);Artist Name(s);Album Name\nspotify:track:1;Song;spotify:artist:1;Artist;Album"
        assertEquals(CsvRawTrack("Song", "Artist", "Album"), importer.parseTracks(csv, "songs.csv").single())
        assertEquals(CsvRawTrack("Song", "Artist"), importer.parseTracks("Title\tArtist\nSong\tArtist", "songs.tsv").single())
        assertEquals("", importer.parseTracks("Title,Album\nSong,Album", "songs.csv").single().artist)
    }

    @Test
    fun readsTextAndM3uMetadataWithoutStrippingVersions() {
        val text = "Artist, Guest - Song (Live)\nAmbiguous title"
        val rows = importer.parseTracks(text, "songs.txt")
        assertEquals(CsvRawTrack("Song (Live)", "Artist, Guest"), rows.first())
        assertEquals("", rows.last().artist)
        val m3u = "#EXTM3U\n#EXTINF:-1,Artist - Song (Live)\n/path/song.mp3\n#EXTINF:-1,Other - Song 2\n/path/song2.mp3"
        assertEquals(listOf(CsvRawTrack("Song (Live)", "Artist"), CsvRawTrack("Song 2", "Other")), importer.parseTracks(m3u, "songs.m3u"))
        val metadataOnlyM3u = "#EXTM3U\n#EXTINF:-1,Artist - Song\n\n#EXTINF:-1,Other - Song 2\n"
        assertEquals(listOf(CsvRawTrack("Song", "Artist"), CsvRawTrack("Song 2", "Other")), importer.parseTracks(metadataOnlyM3u, "songs.m3u"))
    }

    @Test
    fun skipsUnmatchedRowsAndPinsVerifiedVideoId() = runBlocking {
        coEvery { api.searchSongs(any(), 30, false) } returns listOf(
            YouTubeMusicTrack("abcdefghijk", "Song", "Artist Tribute"),
            YouTubeMusicTrack("12345678901", "Song", "Artist"),
        )
        val result = importer.parseAndMatchCsv("Track,Artist\nSong,Artist\nMissing,Artist".byteInputStream(), "songs.csv")
        assertEquals(2, result.totalRows)
        assertEquals(1, result.matchedCount)
        assertEquals("https://music.youtube.com/watch?v=12345678901", result.tracks.single().url)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMalformedQuotedCsv() {
        importer.parseTracks("Track,Artist\n\"Unclosed,Artist", "songs.csv")
    }
}
