package com.abcg.music.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File

class VoicePlayer(private val context: Context) {

    private var player: MediaPlayer? = null

    fun play(file: File) {
        MediaPlayer.create(context, file.toUri()).apply {
            player = this
            start()
        }
    }
    
    fun play(url: String) {
        player?.release()
        player = MediaPlayer().apply {
            try {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { start() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}
