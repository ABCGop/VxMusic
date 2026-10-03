package com.abcg.music.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abcg.music.data.local.AccentMode
import com.abcg.music.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    val uiState = themeRepository.uiState

    fun setManualAccent(color: Color) = viewModelScope.launch {
        themeRepository.setManualAccent(color)
    }

    fun setMode(mode: AccentMode) = viewModelScope.launch {
        themeRepository.setMode(mode)
    }

    fun setAmoled(enabled: Boolean) = viewModelScope.launch {
        themeRepository.setAmoled(enabled)
    }
}
