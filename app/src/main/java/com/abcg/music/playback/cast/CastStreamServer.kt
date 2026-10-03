package com.abcg.music.playback.cast

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.PowerManager
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceInputStream
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import com.abcg.music.playback.MusicPlayer
import fi.iki.elonen.NanoHTTPD
import java.net.Inet4Address
import java.util.UUID

/** Shares only the selected stream, through an unguessable URL on the local network. */
@OptIn(UnstableApi::class)
internal class CastStreamServer(private val context: Context, private val address: String) : NanoHTTPD(address, 0) {
    private val wakeLock = context.getSystemService(PowerManager::class.java)
        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LastWave:CastStream")
    private val wifiLock = context.getSystemService(WifiManager::class.java)
        .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "LastWave:CastStream")
    private data class Stream(val path: String, val source: MusicPlayer.ResolvedStream)
    @Volatile private var stream: Stream? = null

    fun publish(source: MusicPlayer.ResolvedStream): String {
        val path = "/${UUID.randomUUID()}"
        stream = Stream(path, source)
        return "http://$address:$listeningPort$path"
    }

    override fun stop() {
        stream = null
        try {
            super.stop()
        } finally {
            if (wakeLock.isHeld) wakeLock.release()
            if (wifiLock.isHeld) wifiLock.release()
        }
    }

    override fun serve(session: IHTTPSession): Response {
        val selected = stream
        if (selected == null || session.uri != selected.path) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
        }
        if (session.method == Method.OPTIONS) return cors(newFixedLengthResponse(""))
        if (session.method != Method.GET && session.method != Method.HEAD) {
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "Method not allowed")
        }
        val source = selected.source
        val factory = DefaultDataSource.Factory(context, DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(source.requestHeaders))
        val dataSource = factory.createDataSource()
        var input: DataSourceInputStream? = null
        try {
            val spec = DataSpec.Builder().setUri(source.url).build()
            val total = dataSource.open(spec)
            dataSource.close()
            if (total == 0L) return cors(newFixedLengthResponse(Response.Status.OK, source.mimeType, ""))
            val range = session.headers["range"]
            val match = range?.let { Regex("bytes=(\\d*)-(\\d*)").matchEntire(it) }
            var start = 0L
            var end = if (total != C.LENGTH_UNSET.toLong()) total - 1 else Long.MAX_VALUE
            if (range != null) {
                if (match == null || total <= 0) return rangeError(total)
                val first = match.groupValues[1]
                val last = match.groupValues[2]
                if (first.isEmpty()) {
                    val suffix = last.toLongOrNull()?.takeIf { it > 0 } ?: return rangeError(total)
                    start = (total - suffix).coerceAtLeast(0)
                } else {
                    start = first.toLongOrNull() ?: return rangeError(total)
                    if (last.isNotEmpty()) end = minOf(last.toLongOrNull() ?: return rangeError(total), end)
                }
                if (start >= total || end < start) return rangeError(total)
            }
            val length = if (total >= 0) end - start + 1 else C.LENGTH_UNSET.toLong()
            input = DataSourceInputStream(factory.createDataSource(), spec.buildUpon()
                .setPosition(start).setLength(length).build())
            val status = if (range == null) Response.Status.OK else Response.Status.PARTIAL_CONTENT
            val response = if (length >= 0) {
                newFixedLengthResponse(status, source.mimeType, input, length)
            } else {
                newChunkedResponse(status, source.mimeType, input)
            }
            response.addHeader("Accept-Ranges", "bytes")
            if (range != null) response.addHeader("Content-Range", "bytes $start-$end/$total")
            return cors(response)
        } catch (error: Exception) {
            input?.close()
            return cors(newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Stream unavailable"))
        } finally {
            dataSource.close()
        }
    }

    private fun rangeError(total: Long): Response = cors(newFixedLengthResponse(
        Response.Status.RANGE_NOT_SATISFIABLE, MIME_PLAINTEXT, "Invalid range",
    )).apply { if (total >= 0) addHeader("Content-Range", "bytes */$total") }

    private fun cors(response: Response): Response = response.apply {
        addHeader("Access-Control-Allow-Origin", "*")
        addHeader("Access-Control-Allow-Headers", "Range")
        addHeader("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
        addHeader("Access-Control-Expose-Headers", "Content-Range, Accept-Ranges, Content-Length")
    }

    companion object {
        fun create(context: Context): CastStreamServer {
            val manager = context.getSystemService(ConnectivityManager::class.java)
            val address = manager.allNetworks.asSequence().filter { network ->
                val caps = manager.getNetworkCapabilities(network)
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
                    caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
            }.flatMap { manager.getLinkProperties(it)?.linkAddresses.orEmpty().asSequence() }
                .map { it.address }.filterIsInstance<Inet4Address>().firstOrNull { !it.isLoopbackAddress }
                ?.hostAddress ?: error("Connect your phone and Chromecast to the same Wi-Fi network")
            val server = CastStreamServer(context, address)
            try {
                server.start(SOCKET_READ_TIMEOUT, true)
                server.wakeLock.acquire()
                server.wifiLock.acquire()
                return server
            } catch (error: Exception) {
                server.stop()
                throw error
            }
        }
    }
}
