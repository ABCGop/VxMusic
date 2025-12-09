package com.abcg.music.ui.screen.icon

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abcg.music.billing.BillingManager
import com.abcg.music.ui.icon.AppIcon
import com.abcg.music.ui.icon.IconManager
import com.maxrave.domain.manager.DataStoreManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IconViewModel(
    private val dataStoreManager: DataStoreManager,
    private val billingManager: BillingManager,
    private val context: Context
) : ViewModel() {

    val currentIconId = dataStoreManager.selectedIconId
        .stateIn(viewModelScope, SharingStarted.Eagerly, IconManager.Default.id)

    val isPro = billingManager.isPro
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val icons = IconManager.icons

    fun selectIcon(icon: AppIcon) {
        viewModelScope.launch {
            dataStoreManager.setSelectedIconId(icon.id)
            IconManager.setIcon(context, icon)
        }
    }
}
