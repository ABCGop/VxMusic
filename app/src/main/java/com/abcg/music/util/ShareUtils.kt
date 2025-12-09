package com.abcg.music.util

import android.content.Context
import android.content.Intent

object ShareUtils {
    fun shareSong(context: Context, songId: String, title: String, artist: String) {
        val url = "https://web.vxmusic.in/s/$songId"
        val text = "Check out $title by $artist on VxMusic! 🎶\n$url"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Song"))
    }
}
