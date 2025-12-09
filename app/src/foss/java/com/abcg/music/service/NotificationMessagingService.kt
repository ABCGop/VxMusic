package com.abcg.music.service

import android.util.Log

/**
 * FOSS flavor stub for NotificationMessagingService
 * This version doesn't have Firebase dependencies
 */
class NotificationMessagingService {
    
    companion object {
        private const val TAG = "FCMService"
        
        fun getToken(): String? {
            Log.d(TAG, "FOSS flavor - Firebase not available")
            return null
        }
    }
}
