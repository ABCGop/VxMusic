package com.abcg.music.lyrics

import android.content.Context
import com.abcg.innertube.YouTube

object YouTubeSubtitleLyricsProvider : LyricsProvider {
    override val name = "YouTube Subtitle"
    override fun isEnabled(context: Context) = true
    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<String> =
        YouTube.transcript(id)
}
