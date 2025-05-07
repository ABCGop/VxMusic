package com.abcg.music.utils

import com.abcg.music.db.entities.LyricsEntity

object TranslationHelper {
    suspend fun translate(lyrics: LyricsEntity): LyricsEntity = lyrics
    suspend fun clearModels() {}
}