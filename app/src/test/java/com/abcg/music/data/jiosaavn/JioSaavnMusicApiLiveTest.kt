package com.abcg.music.data.jiosaavn

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class JioSaavnMusicApiLiveTest {

    private lateinit var api: JioSaavnMusicApi
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setUp() {
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        api = JioSaavnMusicApi(okHttpClient)
    }

    @Test
    fun testDesDecryption() {
        val encrypted = "ID2ieOjCrwfgWvL5sXl4B1ImC5QfbsDyZboUjCc/L8y5sps0emEpN5x62tJIjXxDUOPVCCdRZKy/+EY8t88DXxw7tS9a8Gtq"
        val decrypted = api.decryptMediaUrl(encrypted)
        assertNotNull("Decrypted URL should not be null", decrypted)
        assertTrue("Decrypted URL should be a saavncdn link", decrypted!!.contains("saavncdn.com"))
        val url320 = api.formatAudioUrl(decrypted, 320)
        assertTrue("URL should have _320 suffix", url320.endsWith("_320.mp4") || url320.contains("_320.mp4"))
    }

    @Test
    fun testResolveSkyfall() = runBlocking {
        val stream = api.resolveStream(
            title = "Skyfall",
            artist = "Adele",
        )
        assertNotNull("Skyfall by Adele should resolve on JioSaavn", stream)
        assertEquals(320, stream!!.bitrateKbps)
        assertEquals("audio/mp4", stream.mimeType)
        assertTrue("Stream URL should contain saavncdn", stream.url.contains("saavncdn.com"))

        val req = Request.Builder().url(stream.url).head().build()
        okHttpClient.newCall(req).execute().use { res ->
            assertTrue("Expected HTTP 200/206 for stream", res.isSuccessful)
            println("Verified JioSaavn stream: ${res.code} ${res.header("Content-Type")} length=${res.header("Content-Length")}")
        }
    }
}
