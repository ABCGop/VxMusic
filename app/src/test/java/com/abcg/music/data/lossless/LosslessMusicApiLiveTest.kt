package com.abcg.music.data.lossless

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class LosslessMusicApiLiveTest {

    private lateinit var api: LosslessMusicApi
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setUp() {
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        api = LosslessMusicApi(okHttpClient)
    }

    private fun verifyFlacStream(streamUrl: String) {
        val req = Request.Builder().url(streamUrl).header("User-Agent", "LastWave/1.0").get().build()
        okHttpClient.newCall(req).execute().use { response ->
            assertTrue("Expected HTTP 200/206 but got ${response.code}", response.isSuccessful)
            val contentType = response.header("Content-Type").orEmpty()
            assertTrue("Expected audio/flac but got $contentType", contentType.contains("flac"))
            val source = response.body?.source()
            assertNotNull("Response body source should not be null", source)
            val buffer = okio.Buffer()
            source?.read(buffer, 4096)
            val bytes = buffer.readByteArray()
            val hasFlacHeader = bytes.size >= 4 && bytes[0] == 'f'.code.toByte() && bytes[1] == 'L'.code.toByte() && bytes[2] == 'a'.code.toByte() && bytes[3] == 'C'.code.toByte()
            println("  [Stream Verified] HTTP ${response.code} | Content-Type: $contentType | First ${bytes.size} bytes | Native FLAC Header: $hasFlacHeader")
            assertTrue("Stream should begin with fLaC header", hasFlacHeader)
        }
    }

    @Test
    fun testDoZindigiByDikzYouTubeFormat() = runBlocking {
        println("\n=== KOTLIN TEST: 'Do Zindagi | Dikz (Official Audio)' by 'Dikz - Topic' ===")
        val stream = api.resolveStream(
            title = "Do Zindagi | Dikz (Official Audio)",
            artist = "Dikz - Topic",
        )
        assertNotNull("Do Zindagi YouTube format should resolve to a Lossless stream", stream)
        println("  Resolved Stream URL: ${stream?.url}")
        println("  FormatId: ${stream?.formatId} | MimeType: ${stream?.mimeType}")
        verifyFlacStream(stream!!.url)
    }

    @Test
    fun testAyanokojiByDikzYouTubeFormat() = runBlocking {
        println("\n=== KOTLIN TEST: 'Ayanokoji | Dikz' by 'Dikz - Topic' ===")
        val stream = api.resolveStream(
            title = "Ayanokoji | Dikz",
            artist = "Dikz - Topic",
        )
        assertNotNull("Ayanokoji YouTube format should resolve to a Lossless stream", stream)
        println("  Resolved Stream URL: ${stream?.url}")
        println("  FormatId: ${stream?.formatId} | MimeType: ${stream?.mimeType}")
        verifyFlacStream(stream!!.url)
    }

    @Test
    fun testGojoVsSakunaByDikzYouTubeFormat() = runBlocking {
        println("\n=== KOTLIN TEST: 'Gojo vs Sakuna Rap' by 'Dikz' ===")
        val stream = api.resolveStream(
            title = "Gojo vs Sakuna Rap",
            artist = "Dikz",
        )
        assertNotNull("Gojo vs Sakuna YouTube format should resolve to a Lossless stream", stream)
        println("  Resolved Stream URL: ${stream?.url}")
        println("  FormatId: ${stream?.formatId} | MimeType: ${stream?.mimeType}")
        verifyFlacStream(stream!!.url)
    }

    @Test
    fun testDieWithASmilePureQobuz() = runBlocking {
        println("\n=== KOTLIN TEST: 'Die With A Smile' by 'Lady Gaga & Bruno Mars' ===")
        val stream = api.resolveStream(
            title = "Die With A Smile",
            artist = "Lady Gaga & Bruno Mars",
        )
        assertNotNull("Die With A Smile should resolve to a Lossless stream", stream)
        println("  Resolved Stream URL: ${stream?.url}")
        println("  FormatId: ${stream?.formatId} | MimeType: ${stream?.mimeType}")
        verifyFlacStream(stream!!.url)
    }
}
