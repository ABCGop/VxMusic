package com.maxrave.data.repository

import com.maxrave.domain.repository.UpdateRepository
import com.maxrave.domain.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class UpdateRepositoryImpl : UpdateRepository {
    override fun checkForPlayStoreUpdate(): Flow<Resource<Boolean>> =
        flow {
            // This is a placeholder implementation
            // The actual Play Store update check will be handled in the UI layer
            // because it requires Activity context and AppUpdateManager
            emit(Resource.Success(false))
        }.flowOn(Dispatchers.IO)
}