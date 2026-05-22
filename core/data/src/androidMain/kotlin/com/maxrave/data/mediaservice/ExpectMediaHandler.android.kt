package com.maxrave.data.mediaservice

import com.maxrave.domain.repository.AnalyticsRepository

actual fun createMediaServiceHandler(
    dataStoreManager: com.maxrave.domain.manager.DataStoreManager,
    songRepository: com.maxrave.domain.repository.SongRepository,
    streamRepository: com.maxrave.domain.repository.StreamRepository,
    localPlaylistRepository: com.maxrave.domain.repository.LocalPlaylistRepository,
    analyticsRepository: AnalyticsRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
): com.maxrave.domain.mediaservice.handler.MediaPlayerHandler =
    MediaServiceHandlerImpl(
        dataStoreManager,
        songRepository,
        streamRepository,
        localPlaylistRepository,
        analyticsRepository,
        coroutineScope,
    )