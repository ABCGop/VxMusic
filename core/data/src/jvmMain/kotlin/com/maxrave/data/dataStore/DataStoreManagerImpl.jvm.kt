package com.maxrave.data.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.maxrave.common.SETTINGS_FILENAME
import com.maxrave.data.io.getHomeFolderPath
import createDataStore
import java.io.File

actual fun createDataStoreInstance(): DataStore<Preferences> = createDataStore(
    producePath = {
        val file = File(getHomeFolderPath(listOf(".vxmusic")), "$SETTINGS_FILENAME.preferences_pb")
        file.absolutePath
    }
)