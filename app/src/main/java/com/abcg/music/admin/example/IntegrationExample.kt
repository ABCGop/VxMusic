package com.abcg.music.admin.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abcg.music.admin.AdminViewModel
import com.abcg.music.admin.ui.BlockedScreen
import com.abcg.music.admin.ui.NotificationBadge
import com.abcg.music.admin.ui.PopupDialog

/**
 * Example integration of AdminViewModel into your MainActivity
 * 
 * This shows you how to:
 * 1. Initialize the AdminViewModel
 * 2. Handle blocked users
 * 3. Show popups
 * 4. Use dynamic theme colors
 * 5. Check feature flags
 * 6. Display notification badges
 */
class ExampleMainActivity : ComponentActivity() {
    
    private lateinit var adminViewModel: AdminViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AdminViewModel
        adminViewModel = AdminViewModel(applicationContext)
        
        setContent {
            AdminIntegratedApp(adminViewModel)
        }
    }
}

@Composable
fun AdminIntegratedApp(adminViewModel: AdminViewModel) {
    // Observe block status
    val isBlocked by adminViewModel.isBlocked.collectAsState()
    
    // Observe current popup
    val currentPopup by adminViewModel.currentPopup.collectAsState()
    
    // Observe theme config
    val themeConfig by adminViewModel.themeConfig.collectAsState()
    val primaryColor = adminViewModel.getPrimaryColor()
    val secondaryColor = adminViewModel.getSecondaryColor()
    
    // Apply dynamic theme
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            // Add your other colors here
        )
    ) {
        // Check if user is blocked
        if (isBlocked) {
            // Show blocked screen - user can't access the app
            BlockedScreen()
        } else {
            // Normal app content
            YourNormalAppContent(adminViewModel)
            
            // Show popup if exists
            currentPopup?.let { popup ->
                PopupDialog(
                    popup = popup,
                    onDismiss = { adminViewModel.dismissPopup() },
                    onAction = { url -> 
                        adminViewModel.handlePopupAction(url)
                        // You can also handle custom actions here
                        // e.g., navigate to a screen based on URL
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourNormalAppContent(adminViewModel: AdminViewModel) {
    val notifications by adminViewModel.notifications.collectAsState()
    val unreadCount by adminViewModel.unreadNotificationCount.collectAsState()
    val featureFlags by adminViewModel.featureFlags.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VxMusic") },
                actions = {
                    // Show notification icon with badge
                    Box {
                        IconButton(onClick = { /* Open notifications */ }) {
                            Text("🔔")
                        }
                        // Show badge if there are unread notifications
                        NotificationBadge(
                            count = unreadCount,
                            modifier = Modifier.offset(x = 24.dp, y = 8.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Welcome to VxMusic!",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Example: Check feature flags before showing features
            if (featureFlags.aiLyrics) {
                Button(onClick = { openAiLyrics() }) {
                    Text("🎤 AI Lyrics")
                }
            }
            
            if (featureFlags.spotify) {
                Button(onClick = { openSpotify() }) {
                    Text("🎵 Connect Spotify")
                }
            }
            
            if (featureFlags.downloads) {
                Button(onClick = { downloadSong(adminViewModel) }) {
                    Text("⬇️ Download")
                }
            } else {
                Button(
                    onClick = { },
                    enabled = false
                ) {
                    Text("⬇️ Downloads Disabled")
                }
            }
            
            // Show theme info
            Text(
                text = "Current Theme: ${adminViewModel.themeConfig.value.primaryColor}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Example function that checks feature flag before executing
fun downloadSong(adminViewModel: AdminViewModel) {
    if (!adminViewModel.isFeatureEnabled("downloads")) {
        // Show toast or dialog
        println("Downloads are currently disabled by admin")
        return
    }
    
    // Proceed with download
    println("Downloading song...")
}

fun openAiLyrics() {
    println("Opening AI Lyrics...")
}

fun openSpotify() {
    println("Opening Spotify...")
}

/**
 * Example: How to check feature flags in your existing code
 */
class ExampleMusicPlayer(private val adminViewModel: AdminViewModel) {
    
    fun downloadTrack(trackId: String) {
        // Check if downloads are enabled
        if (!adminViewModel.isFeatureEnabled("downloads")) {
            showMessage("Downloads are currently unavailable")
            return
        }
        
        // Proceed with download
        println("Downloading track: $trackId")
    }
    
    fun shareToSocial(trackId: String) {
        // Check if social sharing is enabled
        if (!adminViewModel.isFeatureEnabled("socialShare")) {
            showMessage("Social sharing is currently unavailable")
            return
        }
        
        // Proceed with sharing
        println("Sharing track: $trackId")
    }
    
    fun showLyrics(trackId: String) {
        // Check if AI lyrics are enabled
        if (!adminViewModel.isFeatureEnabled("aiLyrics")) {
            showMessage("Lyrics are currently unavailable")
            return
        }
        
        // Proceed with showing lyrics
        println("Showing lyrics for: $trackId")
    }
    
    private fun showMessage(message: String) {
        println(message)
        // Show toast or snackbar in your actual implementation
    }
}

/**
 * Example: How to handle notifications in your app
 */
@Composable
fun NotificationsScreen(adminViewModel: AdminViewModel) {
    val notifications by adminViewModel.notifications.collectAsState()
    
    LazyColumn {
        items(notifications.size) { index ->
            val notification = notifications[index]
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (notification.read) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row {
                        if (!notification.read) {
                            TextButton(
                                onClick = { 
                                    adminViewModel.markNotificationAsRead(notification.notificationId)
                                }
                            ) {
                                Text("Mark as Read")
                            }
                        }
                        
                        TextButton(
                            onClick = { 
                                adminViewModel.deleteNotification(notification.notificationId)
                            }
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Example: Initialize AdminViewModel using Koin (if you use dependency injection)
 */
/*
val appModule = module {
    single { AdminViewModel(androidContext()) }
}

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}
*/
