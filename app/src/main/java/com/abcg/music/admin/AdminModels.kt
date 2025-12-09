package com.abcg.music.admin

data class AdminConfig(
    val theme: ThemeConfig? = null,
    val features: FeatureFlags? = null,
    val blockedUsers: Map<String, Boolean>? = null
)

data class ThemeConfig(
    val primaryColor: String = "#8B5CF6",
    val secondaryColor: String = "#3B82F6",
    val updatedAt: Long = 0L
)

data class FeatureFlags(
    val aiLyrics: Boolean = true,
    val spotify: Boolean = true,
    val ytMusic: Boolean = true,
    val downloads: Boolean = true,
    val socialShare: Boolean = true
)

data class AppNotification(
    val notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val read: Boolean = false
)

data class AppPopup(
    val popupId: String = "",
    val type: PopupType = PopupType.INFO,
    val title: String = "",
    val message: String = "",
    val buttonText: String = "OK",
    val actionUrl: String? = null,
    val dismissible: Boolean = true,
    val timestamp: Long = 0L,
    val shown: Boolean = false
)

enum class PopupType {
    INFO, WARNING, UPDATE, PROMO
}

data class UserProfile(
    val userId: String = "",
    val blocked: Boolean = false,
    val blockedAt: Long? = null,
    val lastSeen: Long = System.currentTimeMillis()
)
