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
        val title = data["title"] ?: return
        val body = data["body"] ?: ""
        val imageUrl = data["image_url"]
        val clickAction = data["click_action"]
        val deepLink = data["deep_link"]
        val notificationType = data["type"]
        
        when (notificationType) {
            "announcement" -> sendNotification(title, body, imageUrl, clickAction, deepLink)
            "update_available" -> sendNotification(title, body, imageUrl, "UPDATE", null)
            "new_feature" -> sendNotification(title, body, imageUrl, "FEATURE", deepLink)
            "promotion" -> sendNotification(title, body, imageUrl, "PROMO", deepLink)
            else -> sendNotification(title, body, imageUrl, clickAction, deepLink)
        }
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
