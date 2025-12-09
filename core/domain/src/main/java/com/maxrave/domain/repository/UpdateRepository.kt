package com.maxrave.domain.repository

import com.maxrave.domain.utils.Resource
import kotlinx.coroutines.flow.Flow

interface UpdateRepository {
    fun checkForPlayStoreUpdate(): Flow<Resource<Boolean>>
}