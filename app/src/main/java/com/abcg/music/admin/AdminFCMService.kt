package com.abcg.music.admin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AdminFCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "AdminFCMService"
        private const val CHANNEL_ID = "vxmusic_admin"
        private const val CHANNEL_NAME = "VxMusic Admin Notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // Subscribe to 'all_users' topic
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to all_users topic")
                } else {
                    Log.e(TAG, "Failed to subscribe to topic", task.exception)
                }
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message received from: ${message.from}")
        
        val data = message.data
        val type = data["type"] ?: "unknown"
        
        when (type) {
            "notification" -> handleNotification(data, message.notification)
            "popup" -> handlePopup(data)
            "theme_update" -> handleThemeUpdate(data)
            "feature_toggle" -> handleFeatureToggle(data)
            else -> Log.w(TAG, "Unknown message type: $type")
        }
    }

    private fun handleNotification(data: Map<String, String>, notification: RemoteMessage.Notification?) {
        val title = data["title"] ?: notification?.title ?: "VxMusic"
        val message = data["message"] ?: notification?.body ?: ""
        
        createNotificationChannel()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        
        Log.d(TAG, "Notification displayed: $title")
    }

    private fun handlePopup(data: Map<String, String>) {
        val title = data["title"] ?: ""
        val message = data["message"] ?: ""
        val popupType = data["popup_type"] ?: "info"
        
        Log.d(TAG, "Popup received: $title - $message (type: $popupType)")
        
        // Show as notification for now (you can implement dialog in app later)
        createNotificationChannel()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
        
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun handleThemeUpdate(data: Map<String, String>) {
        val primaryColor = data["primary_color"] ?: "#8B5CF6"
        val secondaryColor = data["secondary_color"] ?: "#3B82F6"
        val accentColor = data["accent_color"] ?: "#10B981"
        
        Log.d(TAG, "Theme update: $primaryColor / $secondaryColor / $accentColor")
        
        // Save theme to SharedPreferences
        val prefs = getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("primary_color", primaryColor)
            .putString("secondary_color", secondaryColor)
            .putString("accent_color", accentColor)
            .putLong("theme_updated_at", System.currentTimeMillis())
            .apply()
        
        // Broadcast to app (app should listen for this)
        val intent = Intent("com.abcg.music.THEME_UPDATED")
        sendBroadcast(intent)
    }

    private fun handleFeatureToggle(data: Map<String, String>) {
        val featureKey = data["feature_key"] ?: ""
        val enabled = data["enabled"]?.toBoolean() ?: true
        
        Log.d(TAG, "Feature toggle: $featureKey = $enabled")
        
        // Save feature state to SharedPreferences
        val prefs = getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("feature_$featureKey", enabled)
            .putLong("feature_${featureKey}_updated_at", System.currentTimeMillis())
            .apply()
        
        // Broadcast to app (app should listen for this)
        val intent = Intent("com.abcg.music.FEATURE_TOGGLED")
        intent.putExtra("feature_key", featureKey)
        intent.putExtra("enabled", enabled)
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from VxMusic admin"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
