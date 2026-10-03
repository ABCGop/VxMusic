package com.abcg.music.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.abcg.music.MainActivity
import com.abcg.music.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class VxFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token received: $token")
        // Cache token or send to your server if needed
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        // 1. Resolve notification title and body
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: getString(R.string.app_name)

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: ""

        if (title.isBlank() && body.isBlank()) {
            return
        }

        // 2. Resolve target intent (support deep link url or custom payload)
        val deepLinkUrl = remoteMessage.data["url"] ?: remoteMessage.data["deep_link"]
        val intent = if (!deepLinkUrl.isNullOrBlank()) {
            Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUrl)).apply {
                setClass(this@VxFirebaseMessagingService, MainActivity::class.java)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        // Pass all data payload items as extras
        for ((key, value) in remoteMessage.data) {
            intent.putExtra(key, value)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (System.currentTimeMillis() % 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Ensure notification channel exists
        createNotificationChannelIfNeeded()

        // 4. Build notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        notificationManager?.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationManager?.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager?.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        private const val TAG = "VxFirebaseMessaging"
        const val CHANNEL_ID = "vxmusic_announcements"
        const val CHANNEL_NAME = "VxMusic Announcements"
        const val CHANNEL_DESC = "Notifications and updates from VxMusic"
    }
}
