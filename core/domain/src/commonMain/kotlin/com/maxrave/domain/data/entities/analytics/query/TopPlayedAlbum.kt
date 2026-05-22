package com.maxrave.domain.data.entities.analytics.query

import androidx.room.ColumnInfo

data class TopPlayedAlbum(
    @ColumnInfo(name = "albumBrowseId") val albumBrowseId: String,
    @ColumnInfo(name = "playCount") val playCount: Int,
)