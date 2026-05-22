package com.maxrave.data.mediaservice

import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.mediaservice.handler.MediaPlayerHandler
import com.maxrave.domain.repository.AnalyticsRepository
import com.maxrave.domain.repository.LocalPlaylistRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.repository.StreamRepository
import kotlinx.coroutines.CoroutineScope

expect fun createMediaServiceHandler(
    dataStoreManager: DataStoreManager,
    songRepository: SongRepository,
    streamRepository: StreamRepository,
    localPlaylistRepository: LocalPlaylistRepository,
    analyticsRepository: AnalyticsRepository,
    coroutineScope: CoroutineScope,
): MediaPlayerHandler