package com.abcg.music.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Remote Control Manager
 * Handles Firebase Realtime Database communication for remote control
 */
class RemoteControlManager(private val context: Context) {
    
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("remote_control", Context.MODE_PRIVATE)
    
    private var deviceId: String = getOrCreateDeviceId()
    private var currentUserId: String? = null
    
    companion object {
        private const val TAG = "RemoteControlManager"
        private const val DEVICE_ID_KEY = "device_id"
        private const val COMMANDS_PATH = "commands"
        private const val DEVICES_PATH = "devices"
        private const val PLAYLISTS_PATH = "playlists"
    }
    
    /**
     * Initialize remote control system
     */
    suspend fun initialize(): Result<String> {
        return try {
            // Sign in anonymously if not authenticated
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            currentUserId = auth.currentUser?.uid
            
            // Register device
            registerDevice()
            
            Result.success(deviceId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize remote control", e)
            Result.failure(e)
        }
    }
    
    /**
     * Listen for remote commands
     */
    fun listenForCommands(): Flow<RemoteCommand> = callbackFlow {
        val commandsRef = database.getReference("$COMMANDS_PATH/$currentUserId/$deviceId")
        
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(RemoteCommand::class.java)?.let { command ->
                    if (!command.executed) {
                        trySend(command)
                        // Mark as executed
                        snapshot.ref.child("executed").setValue(true)
                        snapshot.ref.child("executedAt").setValue(System.currentTimeMillis())
                    }
                }
            }
            
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Command listener cancelled", error.toException())
            }
        }
        
        commandsRef.addChildEventListener(listener)
        
        awaitClose {
            commandsRef.removeEventListener(listener)
        }
    }
    
    /**
     * Update device state
     */
    suspend fun updateDeviceState(state: DeviceState) {
        try {
            val stateRef = database.getReference("$DEVICES_PATH/$currentUserId/$deviceId")
            stateRef.setValue(state.copy(deviceId = deviceId, lastUpdate = System.currentTimeMillis())).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update device state", e)
        }
    }
    
    /**
     * Get current device state from Firebase
     */
    suspend fun getDeviceState(): DeviceState? {
        return try {
            val stateRef = database.getReference("$DEVICES_PATH/$currentUserId/$deviceId")
            val snapshot = stateRef.get().await()
            snapshot.getValue(DeviceState::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get device state", e)
            null
        }
    }
    
    /**
     * Update playlists for remote access
     */
    suspend fun updatePlaylists(playlists: List<RemotePlaylist>) {
        try {
            val playlistsRef = database.getReference("$PLAYLISTS_PATH/$currentUserId/$deviceId")
            playlistsRef.setValue(playlists).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update playlists", e)
        }
    }
    
    /**
     * Send a command from this device (for testing/debugging)
     */
    suspend fun sendCommand(command: RemoteCommand) {
        try {
            val commandId = UUID.randomUUID().toString()
            val commandRef = database.getReference("$COMMANDS_PATH/$currentUserId/$deviceId/$commandId")
            commandRef.setValue(command.copy(commandId = commandId)).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send command", e)
        }
    }
    
    /**
     * Clear old executed commands (cleanup)
     */
    suspend fun clearOldCommands() {
        try {
            val commandsRef = database.getReference("$COMMANDS_PATH/$currentUserId/$deviceId")
            val snapshot = commandsRef.get().await()
            
            val cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 hours
            
            snapshot.children.forEach { child ->
                val command = child.getValue(RemoteCommand::class.java)
                if (command?.executed == true && command.timestamp < cutoffTime) {
                    child.ref.removeValue()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear old commands", e)
        }
    }
    
    /**
     * Get access token for web interface
     */
    suspend fun getAccessToken(): String? {
        return try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get access token", e)
            null
        }
    }
    
    /**
     * Get device ID and user ID for web interface
     */
    fun getConnectionInfo(): Pair<String, String?> {
        return Pair(deviceId, currentUserId)
    }
    
    private fun getOrCreateDeviceId(): String {
        var id = prefs.getString(DEVICE_ID_KEY, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString(DEVICE_ID_KEY, id).apply()
        }
        return id
    }
    
    private suspend fun registerDevice() {
        val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        val initialState = DeviceState(
            deviceId = deviceId,
            deviceName = deviceName,
            isPlaying = false,
            lastUpdate = System.currentTimeMillis()
        )
        updateDeviceState(initialState)
    }
}
