package com.maxrave.data.db.datasource

import DatabaseDao
import com.maxrave.domain.data.entities.analytics.PlaybackEventEntity
import com.maxrave.domain.extension.beforeXDays
import com.maxrave.domain.extension.now
import kotlinx.datetime.LocalDateTime

internal class AnalyticsDatasource(
    private val databaseDao: DatabaseDao,
) {
    suspend fun insertPlaybackEvent(
        videoId: String,
        channelIds: List<String>,
        albumBrowseId: String?,
        durationSecond: Long,
        listenedSecond: Long,
    ) = databaseDao.insertPlaybackWithArtists(
        videoId = videoId,
        channelIds = channelIds,
        albumBrowseId = albumBrowseId,
        durationSecond = durationSecond,
        listenedSecond = listenedSecond,
    )

    suspend fun getPlaybackEventsByOffset(
        offset: Int,
        limit: Int,
    ): List<PlaybackEventEntity> = databaseDao.getPlaybackEventsByOffset(offset, limit)

    suspend fun getPlaybackEventsByOffsetAndTimestamp(
        offset: Int,
        limit: Int,
        cutoffTimestamp: LocalDateTime,
    ): List<PlaybackEventEntity> = databaseDao.getPlaybackEventsByOffsetAndTimestamp(offset, limit, cutoffTimestamp)

    suspend fun deleteOldPlaybackEvents(cutoffTimestamp: LocalDateTime) = databaseDao.deleteOldPlaybackEvents(cutoffTimestamp)

    // Query methods for analytics reports
    suspend fun queryTopPlayedSongsLastXDays(x: Int) =
        databaseDao.queryTopPlayedSongsInRange(
            startTimestamp = now().beforeXDays(x),
            endTimestamp = now(),
        )

    suspend fun queryTopPlayedSongsInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ) = databaseDao.queryTopPlayedSongsInRange(startTimestamp, endTimestamp)

    suspend fun queryTopArtistsLastXDays(x: Int) =
        databaseDao.queryTopArtistsInRange(
            startTimestamp = now().beforeXDays(x),
            endTimestamp = now(),
        )

    suspend fun queryTopArtistsInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ) = databaseDao.queryTopArtistsInRange(startTimestamp, endTimestamp)

    suspend fun queryTopAlbumsLastXDays(x: Int) =
        databaseDao.queryTopAlbumsInRange(
            startTimestamp = now().beforeXDays(x),
            endTimestamp = now(),
        )

    suspend fun queryTopAlbumsInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ) = databaseDao.queryTopAlbumsInRange(startTimestamp, endTimestamp)

    suspend fun getTotalPlaybackEventCount(): Long = databaseDao.getTotalPlaybackEventCount()

    suspend fun getTotalEventArtistCount(): Long = databaseDao.getTotalEventArtistCount()

    suspend fun getTotalListeningTimeInSeconds(): Long = databaseDao.getTotalListeningTimeInSeconds()

    suspend fun getPlaybackEventCountInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ) = databaseDao.getPlaybackEventCountInRange(
        startTimestamp,
        endTimestamp,
    )
}