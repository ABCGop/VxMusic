package com.abcg.music.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.abcg.music.data.model.metadata.Line

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey(autoGenerate = false) val videoId: String,
    val error: Boolean,
    val lines: List<Line>?,
    val syncType: String?,
)