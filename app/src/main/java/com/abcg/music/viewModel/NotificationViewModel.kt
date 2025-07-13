package com.abcg.music.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.abcg.music.data.db.entities.NotificationEntity
import com.abcg.music.viewModel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@UnstableApi
class NotificationViewModel(
    application: Application,
) : BaseViewModel(application) {
    private var _listNotification: MutableStateFlow<List<NotificationEntity>?> =
        MutableStateFlow(null)
    val listNotification: StateFlow<List<NotificationEntity>?> = _listNotification

    init {
        viewModelScope.launch {
            mainRepository.getAllNotifications().collect { notificationEntities ->
                _listNotification.value =
                    notificationEntities?.sortedByDescending {
                        it.time
                    }
            }
        }
    }
}