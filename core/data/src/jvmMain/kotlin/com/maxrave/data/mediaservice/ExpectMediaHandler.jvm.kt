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
    JvmMediaPlayerHandlerImpl(
        dataStoreManager = dataStoreManager,
        songRepository = songRepository,
        streamRepository = streamRepository,
        localPlaylistRepository = localPlaylistRepository,
        analyticsRepository = analyticsRepository,
        coroutineScope = coroutineScope,
    )