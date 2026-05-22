package com.maxrave.domain.data.entities.analytics

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "event_artist",
    primaryKeys = ["eventId", "channelId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaybackEventEntity::class,
            parentColumns = ["eventId"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["channelId"]),
        Index(value = ["timestamp", "channelId"])
    ]
)
data class EventArtistEntity(
    val eventId: Long,
    val channelId: String,
    val timestamp: LocalDateTime
)