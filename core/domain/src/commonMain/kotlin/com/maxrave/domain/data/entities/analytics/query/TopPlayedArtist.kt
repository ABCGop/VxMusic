package com.maxrave.domain.data.entities.analytics.query

import androidx.room.ColumnInfo

data class TopPlayedArtist(
    @ColumnInfo(name = "channelId") val channelId: String,
    @ColumnInfo(name = "playCount") val playCount: Int,
)