package com.abcg.music.ui.settings

import androidx.lifecycle.ViewModel
import com.abcg.music.service.ScrobbleDebugLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ScrobblerDebugLogViewModel @Inject constructor(
    private val debugLog: ScrobbleDebugLog,
) : ViewModel() {
    val entries: StateFlow<List<String>> = debugLog.entries

    fun clear() = debugLog.clear()
}
