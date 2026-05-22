package com.maxrave.domain.data.entities.analytics.query

import androidx.room.ColumnInfo

data class TopPlayedTracks(
    @ColumnInfo(name = "videoId") val videoId: String,
    @ColumnInfo(name = "playCount") val playCount: Int = 0,
    @ColumnInfo(name = "totalListeningTime") val totalListeningTime: Long,
)