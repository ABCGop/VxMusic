package com.abcg.music.ui.screen.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abcg.music.billing.BillingManager
import com.abcg.music.data.repository.ThemeRepository
import com.abcg.music.ui.theme.ThemeManager
import com.maxrave.domain.model.AppTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val themeRepository: ThemeRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    val currentTheme = themeRepository.selectedTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeManager.Default)

    val isPro = billingManager.isPro
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val themes = ThemeManager.themes

    fun selectTheme(theme: AppTheme) {
        viewModelScope.launch {
            themeRepository.selectTheme(theme.id)
        }
    }
}
