package com.maxrave.domain.repository

import com.maxrave.domain.data.entities.analytics.PlaybackEventEntity
import com.maxrave.domain.data.entities.analytics.query.TopPlayedAlbum
import com.maxrave.domain.data.entities.analytics.query.TopPlayedArtist
import com.maxrave.domain.data.entities.analytics.query.TopPlayedTracks
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface AnalyticsRepository {
    suspend fun insertPlaybackEvent(
        videoId: String,
        channelIds: List<String>,
        albumBrowseId: String?,
        durationSecond: Long,
        listenedSecond: Long,
    ): Flow<Long>

    suspend fun getPlaybackEventsByOffset(
        offset: Int,
        limit: Int,
    ): Flow<List<PlaybackEventEntity>>

    suspend fun getPlaybackEventsByOffsetAndTimestamp(
        offset: Int,
        limit: Int,
        cutoffTimestamp: LocalDateTime,
    ): Flow<List<PlaybackEventEntity>>

    suspend fun deleteOldPlaybackEvents(cutoffTimestamp: LocalDateTime)

    // Query methods for analytics reports
    suspend fun queryTopPlayedSongsLastXDays(x: Int): Flow<List<TopPlayedTracks>>

    suspend fun queryTopPlayedSongsInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ): Flow<List<TopPlayedTracks>>

    suspend fun queryTopArtistsLastXDays(x: Int): Flow<List<TopPlayedArtist>>

    suspend fun queryTopArtistsInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ): Flow<List<TopPlayedArtist>>

    suspend fun queryTopAlbumsLastXDays(x: Int): Flow<List<TopPlayedAlbum>>

    suspend fun queryTopAlbumsInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ): Flow<List<TopPlayedAlbum>>

    suspend fun getTotalPlaybackEventCount(): Flow<Long>

    suspend fun getTotalEventArtistCount(): Flow<Long>

    suspend fun getTotalListeningTimeInSeconds(): Flow<Long>

    suspend fun getPlaybackEventCountInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ): Flow<Long>
}