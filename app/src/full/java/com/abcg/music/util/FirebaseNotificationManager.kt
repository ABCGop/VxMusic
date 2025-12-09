package com.abcg.music.util

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Manages Firebase Cloud Messaging tokens and subscriptions
 * Full flavor implementation with Firebase
 */
class FirebaseNotificationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "FirebaseNotificationMgr"
        private const val PREF_NAME = "fcm_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
    }
    
    /**
     * Get the current FCM token
     */
    suspend fun getToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM Token retrieved: $token")
            saveToken(token)
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }
    
    /**
     * Get token asynchronously with callback
     */
    fun getTokenAsync(callback: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                callback(null)
                return@OnCompleteListener
            }
            
            val token = task.result
            Log.d(TAG, "FCM Token: $token")
            saveToken(token)
            callback(token)
        })
    }
    
    /**
     * Subscribe to a topic
     */
    suspend fun subscribeToTopic(topic: String): Boolean {
        return try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            Log.d(TAG, "Subscribed to topic: $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to subscribe to topic: $topic", e)
            false
        }
    }
    
    /**
     * Unsubscribe from a topic
     */
    suspend fun unsubscribeFromTopic(topic: String): Boolean {
        return try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            Log.d(TAG, "Unsubscribed from topic: $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unsubscribe from topic: $topic", e)
            false
        }
    }
    
    /**
     * Save token to SharedPreferences
     */
    private fun saveToken(token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
    }
    
    /**
     * Get saved token from SharedPreferences
     */
    fun getSavedToken(): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FCM_TOKEN, null)
    }
    
    /**
     * Delete token (useful for logout or privacy features)
     */
    suspend fun deleteToken(): Boolean {
        return try {
            FirebaseMessaging.getInstance().deleteToken().await()
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_FCM_TOKEN)
                .apply()
            Log.d(TAG, "FCM Token deleted")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete FCM token", e)
            false
        }
    }
    
    /**
     * Subscribe to default topics for all users
     */
    suspend fun subscribeToDefaultTopics() {
        val topics = listOf(
            "all_users",
            "announcements",
            "updates"
        )
        
        topics.forEach { topic ->
            subscribeToTopic(topic)
        }
    }
}
