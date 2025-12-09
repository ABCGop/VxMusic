package com.abcg.music.admin

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class AdminControlManager(private val context: Context) {
    
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
    }
    
    private val userId: String
        get() = auth.currentUser?.uid ?: prefs.getString("user_id", null) ?: ""
    
    companion object {
        private const val TAG = "AdminControlManager"
        private const val PREF_PRIMARY_COLOR = "primary_color"
        private const val PREF_SECONDARY_COLOR = "secondary_color"
        private const val PREF_LAST_THEME_UPDATE = "last_theme_update"
    }
    
    // Initialize user profile
    suspend fun initializeUser() {
        if (userId.isEmpty()) {
            // Anonymous sign in
            try {
                auth.signInAnonymously().await()
                val newUserId = auth.currentUser?.uid ?: return
                prefs.edit().putString("user_id", newUserId).apply()
                
                // Create user profile
                database.getReference("users/$newUserId").setValue(
                    UserProfile(
                        userId = newUserId,
                        blocked = false,
                        lastSeen = System.currentTimeMillis()
                    )
                ).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize user", e)
            }
        }
        
        // Update last seen
        updateLastSeen()
    }
    
    // Check if user is blocked
    fun isUserBlocked(): Flow<Boolean> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(false)
            close()
            return@callbackFlow
        }
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(UserProfile::class.java)
                trySend(profile?.blocked ?: false)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to check block status", error.toException())
                trySend(false)
            }
        }
        
        val ref = database.getReference("users/$userId")
        ref.addValueEventListener(listener)
        
        awaitClose { ref.removeEventListener(listener) }
    }
    
    // Listen for theme updates
    fun observeThemeUpdates(): Flow<ThemeConfig> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val theme = snapshot.getValue(ThemeConfig::class.java) ?: ThemeConfig()
                
                // Only update if it's newer than cached
                val lastUpdate = prefs.getLong(PREF_LAST_THEME_UPDATE, 0L)
                if (theme.updatedAt > lastUpdate) {
                    // Save to prefs for offline use
                    prefs.edit()
                        .putString(PREF_PRIMARY_COLOR, theme.primaryColor)
                        .putString(PREF_SECONDARY_COLOR, theme.secondaryColor)
                        .putLong(PREF_LAST_THEME_UPDATE, theme.updatedAt)
                        .apply()
                }
                
                trySend(theme)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to observe theme updates", error.toException())
            }
        }
        
        val ref = database.getReference("config/theme")
        ref.addValueEventListener(listener)
        
        awaitClose { ref.removeEventListener(listener) }
    }
    
    // Get cached theme
    fun getCachedTheme(): ThemeConfig {
        return ThemeConfig(
            primaryColor = prefs.getString(PREF_PRIMARY_COLOR, "#8B5CF6") ?: "#8B5CF6",
            secondaryColor = prefs.getString(PREF_SECONDARY_COLOR, "#3B82F6") ?: "#3B82F6",
            updatedAt = prefs.getLong(PREF_LAST_THEME_UPDATE, 0L)
        )
    }
    
    // Listen for feature toggles
    fun observeFeatureFlags(): Flow<FeatureFlags> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val features = snapshot.getValue(FeatureFlags::class.java) ?: FeatureFlags()
                trySend(features)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to observe feature flags", error.toException())
            }
        }
        
        val ref = database.getReference("config/features")
        ref.addValueEventListener(listener)
        
        awaitClose { ref.removeEventListener(listener) }
    }
    
    // Listen for notifications (broadcast to all users)
    fun observeNotifications(): Flow<List<AppNotification>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifications = mutableListOf<AppNotification>()
                snapshot.children.forEach { child ->
                    child.getValue(AppNotification::class.java)?.let { notification ->
                        notifications.add(notification.copy(notificationId = child.key ?: ""))
                    }
                }
                trySend(notifications.sortedByDescending { it.timestamp })
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to observe notifications", error.toException())
            }
        }
        
        // Listen to broadcast channel
        val ref = database.getReference("broadcast/notifications")
        ref.addValueEventListener(listener)
        
        awaitClose { ref.removeEventListener(listener) }
    }
    
    // Listen for popups (broadcast to all users)
    fun observePopups(): Flow<List<AppPopup>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val popups = mutableListOf<AppPopup>()
                snapshot.children.forEach { child ->
                    child.getValue(AppPopup::class.java)?.let { popup ->
                        if (!popup.shown) { // Only show unshown popups
                            popups.add(popup.copy(popupId = child.key ?: ""))
                        }
                    }
                }
                trySend(popups.sortedByDescending { it.timestamp })
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to observe popups", error.toException())
            }
        }
        
        // Listen to broadcast channel
        val ref = database.getReference("broadcast/popups")
        ref.addValueEventListener(listener)
        
        awaitClose { ref.removeEventListener(listener) }
    }
    
    // Mark notification as read
    suspend fun markNotificationAsRead(notificationId: String) {
        if (userId.isEmpty()) return
        try {
            database.getReference("notifications/$userId/$notificationId/read")
                .setValue(true)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark notification as read", e)
        }
    }
    
    // Mark popup as shown
    suspend fun markPopupAsShown(popupId: String) {
        if (userId.isEmpty()) return
        try {
            database.getReference("popups/$userId/$popupId/shown")
                .setValue(true)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark popup as shown", e)
        }
    }
    
    // Delete notification
    suspend fun deleteNotification(notificationId: String) {
        if (userId.isEmpty()) return
        try {
            database.getReference("notifications/$userId/$notificationId")
                .removeValue()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete notification", e)
        }
    }
    
    // Update last seen timestamp
    private suspend fun updateLastSeen() {
        if (userId.isEmpty()) return
        try {
            database.getReference("users/$userId/lastSeen")
                .setValue(System.currentTimeMillis())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update last seen", e)
        }
    }
    
    // Convert hex color to Compose Color
    fun hexToColor(hex: String): Color {
        return try {
            val color = android.graphics.Color.parseColor(hex)
            Color(color)
        } catch (e: Exception) {
            Color(0xFF8B5CF6) // Default purple
        }
    }
    
    // Check if a feature is enabled
    suspend fun isFeatureEnabled(feature: String): Boolean {
        return try {
            val snapshot = database.getReference("config/features/$feature")
                .get()
                .await()
            snapshot.getValue(Boolean::class.java) ?: true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check feature: $feature", e)
            true // Default to enabled if check fails
        }
    }
    
    fun cleanup() {
        // Clean up listeners if needed
    }
}
