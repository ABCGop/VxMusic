package com.abcg.music.sdui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@androidx.annotation.Keep
class DynamicUiViewModel(
    private val repository: DynamicUiRepository
) : ViewModel() {
    
    private val _homeDynamicContent = MutableStateFlow<DynamicScreen?>(null)
    val homeDynamicContent: StateFlow<DynamicScreen?> = _homeDynamicContent.asStateFlow()
    
    init {
        loadHomeScreen()
    }
    
    private fun loadHomeScreen() {
        viewModelScope.launch {
            repository.getDynamicScreen("home_screen").collect { screen ->
                _homeDynamicContent.value = screen
            }
        }
    }
}
