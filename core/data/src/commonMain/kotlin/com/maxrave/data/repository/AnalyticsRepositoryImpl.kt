package com.maxrave.data.repository

import com.maxrave.data.db.datasource.AnalyticsDatasource
import com.maxrave.domain.data.entities.analytics.PlaybackEventEntity
import com.maxrave.domain.data.entities.analytics.query.TopPlayedAlbum
import com.maxrave.domain.data.entities.analytics.query.TopPlayedArtist
import com.maxrave.domain.data.entities.analytics.query.TopPlayedTracks
import com.maxrave.domain.repository.AnalyticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime

private const val TAG = "AnalyticsRepositoryImpl"

internal class AnalyticsRepositoryImpl(
    private val analyticsDatasource: AnalyticsDatasource,
) : AnalyticsRepository {
    override suspend fun insertPlaybackEvent(
        videoId: String,
        channelIds: List<String>,
        albumBrowseId: String?,
        durationSecond: Long,
        listenedSecond: Long,
    ): Flow<Long> =
        flow {
            emit(
                analyticsDatasource.insertPlaybackEvent(
                    videoId,
                    channelIds,
                    albumBrowseId,
                    durationSecond,
                    listenedSecond,
                ),
            )
        }.flowOn(Dispatchers.IO)

    override suspend fun getPlaybackEventsByOffset(
        offset: Int,
        limit: Int,
    ): Flow<List<PlaybackEventEntity>> =
        flow {
            emit(analyticsDatasource.getPlaybackEventsByOffset(offset, limit))
        }.flowOn(Dispatchers.IO)

    override suspend fun getPlaybackEventsByOffsetAndTimestamp(
        offset: Int,
        limit: Int,
        cutoffTimestamp: LocalDateTime,
    ): Flow<List<PlaybackEventEntity>> =
        flow {
            emit(analyticsDatasource.getPlaybackEventsByOffsetAndTimestamp(offset, limit, cutoffTimestamp))
        }.flowOn(Dispatchers.IO)

    override suspend fun deleteOldPlaybackEvents(cutoffTimestamp: LocalDateTime) =
        withContext(Dispatchers.IO) {
            analyticsDatasource.deleteOldPlaybackEvents(cutoffTimestamp)
        }

    // Query methods for analytics reports

    override suspend fun queryTopPlayedSongsLastXDays(x: Int): Flow<List<TopPlayedTracks>> =
        flow {
            emit(analyticsDatasource.queryTopPlayedSongsLastXDays(x))
        }.flowOn(Dispatchers.IO)

    override suspend fun queryTopPlayedSongsInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ): Flow<List<TopPlayedTracks>> =
        flow {
            emit(
                analyticsDatasource.queryTopPlayedSongsInRange(
                    startTimestamp,
                    endTimestamp,
                ),
            )
        }.flowOn(Dispatchers.IO)

    override suspend fun queryTopArtistsLastXDays(x: Int): Flow<List<TopPlayedArtist>> =
        flow {
            emit(analyticsDatasource.queryTopArtistsLastXDays(x))
        }.flowOn(Dispatchers.IO)

    override suspend fun queryTopArtistsInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ): Flow<List<TopPlayedArtist>> =
        flow {
            emit(
                analyticsDatasource.queryTopArtistsInRange(
                    startTimestamp,
                    endTimestamp,
                ),
            )
        }.flowOn(Dispatchers.IO)

    override suspend fun queryTopAlbumsLastXDays(x: Int): Flow<List<TopPlayedAlbum>> =
        flow {
            emit(analyticsDatasource.queryTopAlbumsLastXDays(x))
        }.flowOn(Dispatchers.IO)

    override suspend fun queryTopAlbumsInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ): Flow<List<TopPlayedAlbum>> =
        flow {
            emit(
                analyticsDatasource.queryTopAlbumsInRange(
                    startTimestamp,
                    endTimestamp,
                ),
            )
        }.flowOn(Dispatchers.IO)

    override suspend fun getTotalPlaybackEventCount(): Flow<Long> =
        flow {
            emit(analyticsDatasource.getTotalPlaybackEventCount())
        }.flowOn(Dispatchers.IO)

    override suspend fun getTotalEventArtistCount(): Flow<Long> =
        flow {
            emit(analyticsDatasource.getTotalEventArtistCount())
        }.flowOn(Dispatchers.IO)

    override suspend fun getTotalListeningTimeInSeconds(): Flow<Long> =
        flow {
            emit(analyticsDatasource.getTotalListeningTimeInSeconds())
        }.flowOn(Dispatchers.IO)

    override suspend fun getPlaybackEventCountInRange(
        startTimestamp: LocalDateTime,
        endTimestamp: LocalDateTime,
    ): Flow<Long> =
        flow {
            emit(
                analyticsDatasource.getPlaybackEventCountInRange(
                    startTimestamp,
                    endTimestamp,
                ),
            )
        }.flowOn(Dispatchers.IO)
}