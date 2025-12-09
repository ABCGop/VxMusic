package com.abcg.music.util

import android.content.Context
import android.util.Log

/**
 * FOSS flavor stub for FirebaseNotificationManager
 * This version doesn't have Firebase dependencies
 */
class FirebaseNotificationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "FirebaseNotificationMgr"
    }
    
    suspend fun getToken(): String? {
        Log.d(TAG, "FOSS flavor - Firebase not available")
        return null
    }
    
    fun getTokenAsync(callback: (String?) -> Unit) {
        Log.d(TAG, "FOSS flavor - Firebase not available")
        callback(null)
    }
    
    suspend fun subscribeToTopic(topic: String): Boolean {
        Log.d(TAG, "FOSS flavor - Cannot subscribe to topic: $topic")
        return false
    }
    
    suspend fun unsubscribeFromTopic(topic: String): Boolean {
        Log.d(TAG, "FOSS flavor - Cannot unsubscribe from topic: $topic")
        return false
    }
    
    fun getSavedToken(): String? {
        return null
    }
    
    suspend fun deleteToken(): Boolean {
        return true
    }
    
    suspend fun subscribeToDefaultTopics() {
        Log.d(TAG, "FOSS flavor - Cannot subscribe to topics")
    }
}
