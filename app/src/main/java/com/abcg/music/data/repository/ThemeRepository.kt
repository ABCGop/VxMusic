package com.abcg.music.data.repository

import com.abcg.music.ui.theme.ThemeManager
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeRepository(
    private val dataStoreManager: DataStoreManager
) {

    val selectedTheme: Flow<AppTheme> = dataStoreManager.selectedThemeId.map { themeId ->
        ThemeManager.getThemeById(themeId)
    }

    suspend fun selectTheme(themeId: String) {
        dataStoreManager.setSelectedThemeId(themeId)
    }

    fun getAllThemes(): List<AppTheme> {
        return ThemeManager.themes
    }
}
