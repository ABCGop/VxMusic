package com.maxrave.data.mediaservice

actual fun createMediaServiceHandler(
    dataStoreManager: com.maxrave.domain.manager.DataStoreManager,
    songRepository: com.maxrave.domain.repository.SongRepository,
    streamRepository: com.maxrave.domain.repository.StreamRepository,
    localPlaylistRepository: com.maxrave.domain.repository.LocalPlaylistRepository,
    analyticsRepository: com.maxrave.domain.repository.AnalyticsRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
): com.maxrave.domain.mediaservice.handler.MediaPlayerHandler {
    TODO("Not yet implemented")
}