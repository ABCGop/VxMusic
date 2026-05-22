package com.maxrave.domain.data.entities.analytics

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maxrave.domain.extension.now
import kotlinx.datetime.LocalDateTime

@Entity("playback_event")
data class PlaybackEventEntity(
    @PrimaryKey(autoGenerate = true) val eventId: Long = 0,
    val timestamp: LocalDateTime = now(),
    val videoId: String = "",
    val albumBrowseId: String? = null,
    val durationSecond: Long = 0, // 0 - 100
    val listenedSecond: Long = 0, // in seconds
)