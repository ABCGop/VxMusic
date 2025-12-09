package com.abcg.music.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.abcg.music.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.maxrave.common.R

/**
 * Firebase Cloud Messaging Service for receiving push notifications
 * This allows sending notifications to users without app updates
 */
class NotificationMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "app_notifications"
        private const val CHANNEL_NAME = "App Notifications"
        private const val NOTIFICATION_ID_COUNTER = "notification_id_counter"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        
        // Store token in SharedPreferences or send to your server
        getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
        
        // Subscribe to admin broadcast topic
        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to all_users topic for admin broadcasts")
                }
            }
        
        // TODO: Send token to your backend server
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        // Handle data payload
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            handleDataMessage(message.data)
        }

        // Handle notification payload
        message.notification?.let {
            Log.d(TAG, "Message notification: ${it.title} - ${it.body}")
            sendNotification(
                title = it.title ?: "VxMusic",
                body = it.body ?: "",
                imageUrl = it.imageUrl?.toString(),
                clickAction = message.data["click_action"],
                deepLink = message.data["deep_link"]
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val messageType = data["type"]
        
        when (messageType) {
            "notification" -> handleAdminNotification(data)
            "popup" -> handleAdminPopup(data)
            "theme_update" -> handleThemeUpdate(data)
            "theme_reset" -> handleThemeReset()
            "feature_toggle" -> handleFeatureToggle(data)
            "announcement", "update_available", "new_feature", "promotion" -> {
                val title = data["title"] ?: return
                val body = data["body"] ?: ""
                val imageUrl = data["image_url"]
                val clickAction = data["click_action"]
                val deepLink = data["deep_link"]
                sendNotification(title, body, imageUrl, clickAction, deepLink)
            }
            else -> {
                val title = data["title"] ?: return
                val body = data["body"] ?: ""
                sendNotification(title, body, data["image_url"], data["click_action"], data["deep_link"])
            }
        }
    }
    
    private fun handleAdminNotification(data: Map<String, String>) {
        val title = data["title"] ?: "VxMusic"
        val message = data["message"] ?: ""
        sendNotification(title, message, null, null, null)
        Log.d(TAG, "Admin notification displayed: $title")
    }
    
    private fun handleAdminPopup(data: Map<String, String>) {
        val title = data["title"] ?: ""
        val message = data["message"] ?: ""
        val popupType = data["popup_type"] ?: "info"
        
        // Save popup to SharedPreferences for app to show as dialog
        val prefs = getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
        val popupData = """
            {
                "title": "$title",
                "message": "$message",
                "type": "$popupType",
                "timestamp": ${System.currentTimeMillis()},
                "shown": false
            }
        """.trimIndent()
        
        prefs.edit()
            .putString("pending_popup", popupData)
            .putBoolean("has_pending_popup", true)
            .apply()
        
        // Broadcast to app to show dialog immediately if app is open
        val intent = Intent("com.abcg.music.SHOW_POPUP")
        intent.putExtra("title", title)
        intent.putExtra("message", message)
        intent.putExtra("type", popupType)
        sendBroadcast(intent)
        
        Log.d(TAG, "Admin popup queued: $title")
    }
    
    private fun handleThemeUpdate(data: Map<String, String>) {
        val primaryColor = data["primary_color"] ?: "#8B5CF6"
        val secondaryColor = data["secondary_color"] ?: "#3B82F6"
        val accentColor = data["accent_color"] ?: "#10B981"
        val backgroundColor = data["background_color"]
        val surfaceColor = data["surface_color"]
        val errorColor = data["error_color"]
        
        val prefs = getSharedPreferences("admin_prefs", Context.MODE_PRIVATE).edit()
        prefs.putString("primary_color", primaryColor)
            .putString("secondary_color", secondaryColor)
            .putString("accent_color", accentColor)
            .putLong("theme_updated_at", System.currentTimeMillis())
        
        // Save optional colors if provided
        if (backgroundColor != null) prefs.putString("background_color", backgroundColor)
        if (surfaceColor != null) prefs.putString("surface_color", surfaceColor)
        if (errorColor != null) prefs.putString("error_color", errorColor)
        
        prefs.apply()
        
        sendBroadcast(Intent("com.abcg.music.THEME_UPDATED"))
        Log.d(TAG, "Theme updated: $primaryColor / $secondaryColor")
    }
    
    private fun handleThemeReset() {
        // Clear all custom theme colors from SharedPreferences
        val prefs = getSharedPreferences("admin_prefs", Context.MODE_PRIVATE).edit()
        prefs.remove("primary_color")
            .remove("secondary_color")
            .remove("accent_color")
            .remove("background_color")
            .remove("surface_color")
            .remove("error_color")
            .putLong("theme_updated_at", System.currentTimeMillis())
            .apply()
        
        sendBroadcast(Intent("com.abcg.music.THEME_UPDATED"))
        Log.d(TAG, "Theme reset to defaults")
    }
    
    private fun handleFeatureToggle(data: Map<String, String>) {
        val featureKey = data["feature_key"] ?: return
        val enabled = data["enabled"]?.toBoolean() ?: true
        
        getSharedPreferences("admin_prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("feature_$featureKey", enabled)
            .putLong("feature_${featureKey}_updated_at", System.currentTimeMillis())
            .apply()
        
        val intent = Intent("com.abcg.music.FEATURE_TOGGLED")
        intent.putExtra("feature_key", featureKey)
        intent.putExtra("enabled", enabled)
        sendBroadcast(intent)
        Log.d(TAG, "Feature toggled: $featureKey = $enabled")
    }

    private fun sendNotification(
        title: String,
        body: String,
        imageUrl: String? = null,
        clickAction: String? = null,
        deepLink: String? = null
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            
            // Add click action data
            clickAction?.let { putExtra("notification_action", it) }
            deepLink?.let { putExtra("notification_deep_link", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.monochrome)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        // Load image if provided
        imageUrl?.let {
            try {
                // TODO: Load image using Coil or Glide
                // For now, we'll just show text notification
            } catch (e: Exception) {
                Log.e(TAG, "Error loading notification image", e)
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for app updates, announcements, and new features"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Generate unique notification ID
        val notificationId = getNextNotificationId()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun getNextNotificationId(): Int {
        val prefs = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val currentId = prefs.getInt(NOTIFICATION_ID_COUNTER, 1000)
        val nextId = currentId + 1
        prefs.edit().putInt(NOTIFICATION_ID_COUNTER, nextId).apply()
        return currentId
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Implement your server API call to register this device token
        // Example:
        // val api = RetrofitClient.getInstance()
        // api.registerDevice(token)
        
        Log.d(TAG, "Token should be sent to server: $token")
    }
}
