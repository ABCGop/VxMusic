package com.abcg.music.admin

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminViewModel(context: Context) : ViewModel() {
    
    private val adminManager = AdminControlManager(context)
    
    // User block status
    private val _isBlocked = MutableStateFlow(false)
    val isBlocked: StateFlow<Boolean> = _isBlocked.asStateFlow()
    
    // Theme configuration
    private val _themeConfig = MutableStateFlow(adminManager.getCachedTheme())
    val themeConfig: StateFlow<ThemeConfig> = _themeConfig.asStateFlow()
    
    // Feature flags
    private val _featureFlags = MutableStateFlow(FeatureFlags())
    val featureFlags: StateFlow<FeatureFlags> = _featureFlags.asStateFlow()
    
    // Notifications
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()
    
    // Popups
    private val _currentPopup = MutableStateFlow<AppPopup?>(null)
    val currentPopup: StateFlow<AppPopup?> = _currentPopup.asStateFlow()
    
    // Unread notification count
    val unreadNotificationCount: StateFlow<Int> = _notifications.map { list ->
        list.count { !it.read }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    
    companion object {
        private const val TAG = "AdminViewModel"
    }
    
    init {
        initialize()
    }
    
    private fun initialize() {
        viewModelScope.launch {
            // Initialize user
            adminManager.initializeUser()
            
            // Listen for block status
            adminManager.isUserBlocked()
                .catch { e -> Log.e(TAG, "Error observing block status", e) }
                .collect { blocked ->
                    _isBlocked.value = blocked
                    if (blocked) {
                        // User is blocked, show blocking UI
                        Log.w(TAG, "User is blocked")
                    }
                }
        }
        
        viewModelScope.launch {
            // Listen for theme updates
            adminManager.observeThemeUpdates()
                .catch { e -> Log.e(TAG, "Error observing theme updates", e) }
                .collect { theme ->
                    _themeConfig.value = theme
                    Log.d(TAG, "Theme updated: ${theme.primaryColor}, ${theme.secondaryColor}")
                }
        }
        
        viewModelScope.launch {
            // Listen for feature flags
            adminManager.observeFeatureFlags()
                .catch { e -> Log.e(TAG, "Error observing feature flags", e) }
                .collect { flags ->
                    _featureFlags.value = flags
                    Log.d(TAG, "Feature flags updated: $flags")
                }
        }
        
        viewModelScope.launch {
            // Listen for notifications
            adminManager.observeNotifications()
                .catch { e -> Log.e(TAG, "Error observing notifications", e) }
                .collect { notificationList ->
                    _notifications.value = notificationList
                }
        }
        
        viewModelScope.launch {
            // Listen for popups
            adminManager.observePopups()
                .catch { e -> Log.e(TAG, "Error observing popups", e) }
                .collect { popupList ->
                    // Show the first unshown popup
                    if (popupList.isNotEmpty()) {
                        _currentPopup.value = popupList.first()
                    }
                }
        }
    }
    
    // Check if a feature is enabled
    fun isFeatureEnabled(feature: String): Boolean {
        return when (feature.lowercase()) {
            "ailyrics", "ai_lyrics" -> _featureFlags.value.aiLyrics
            "spotify" -> _featureFlags.value.spotify
            "ytmusic", "yt_music", "youtube" -> _featureFlags.value.ytMusic
            "downloads" -> _featureFlags.value.downloads
            "socialshare", "social_share" -> _featureFlags.value.socialShare
            else -> true
        }
    }
    
    // Mark notification as read
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            adminManager.markNotificationAsRead(notificationId)
        }
    }
    
    // Delete notification
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            adminManager.deleteNotification(notificationId)
        }
    }
    
    // Dismiss popup
    fun dismissPopup() {
        val popup = _currentPopup.value ?: return
        viewModelScope.launch {
            adminManager.markPopupAsShown(popup.popupId)
            _currentPopup.value = null
        }
    }
    
    // Handle popup action
    fun handlePopupAction(actionUrl: String?) {
        dismissPopup()
        
        // TODO: Handle action URL (open browser, navigate to screen, etc.)
        if (actionUrl != null) {
            Log.d(TAG, "Popup action: $actionUrl")
        }
    }
    
    // Get primary color
    fun getPrimaryColor() = adminManager.hexToColor(_themeConfig.value.primaryColor)
    
    // Get secondary color
    fun getSecondaryColor() = adminManager.hexToColor(_themeConfig.value.secondaryColor)
    
    override fun onCleared() {
        super.onCleared()
        adminManager.cleanup()
    }
}
