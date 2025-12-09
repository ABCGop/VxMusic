package com.abcg.music.remote

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Remote Control Service ViewModel
 * Bridges the remote control system with the media player
 */
class RemoteControlViewModel(
    context: Context
) : ViewModel() {
    
    private val remoteControlManager = RemoteControlManager(context)
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _deviceId = MutableStateFlow<String>("")
    val deviceId: StateFlow<String> = _deviceId.asStateFlow()
    
    private val _accessToken = MutableStateFlow<String?>( null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()
    
    companion object {
        private const val TAG = "RemoteControlViewModel"
    }
    
    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        data class Connected(val deviceId: String, val userId: String?) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
    
    /**
     * Initialize remote control
     */
    fun initialize() {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.Connecting
            
            val result = remoteControlManager.initialize()
            
            if (result.isSuccess) {
                val (deviceId, userId) = remoteControlManager.getConnectionInfo()
                _deviceId.value = deviceId
                _connectionState.value = ConnectionState.Connected(deviceId, userId)
                
                // Get access token
                _accessToken.value = remoteControlManager.getAccessToken()
                
                // Start listening for commands
                startListening()
                
                // Clean up old commands
                remoteControlManager.clearOldCommands()
            } else {
                _connectionState.value = ConnectionState.Error(
                    result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }
    
    /**
     * Listen for remote commands
     */
    private fun startListening() {
        viewModelScope.launch {
            remoteControlManager.listenForCommands()
                .catch { e ->
                    Log.e(TAG, "Command stream error", e)
                    _connectionState.value = ConnectionState.Error(e.message ?: "Stream error")
                }
                .collect { command ->
                    Log.d(TAG, "Received command: ${command.command}")
                    handleCommand(command)
                }
        }
    }
    
    /**
     * Handle incoming remote command
     * TODO: Connect to actual media player
     */
    private fun handleCommand(command: RemoteCommand) {
        when (command.command) {
            RemoteCommandType.PLAY -> {
                // TODO: Call media player play()
                Log.d(TAG, "Execute PLAY command")
            }
            RemoteCommandType.PAUSE -> {
                // TODO: Call media player pause()
                Log.d(TAG, "Execute PAUSE command")
            }
            RemoteCommandType.NEXT -> {
                // TODO: Call media player next()
                Log.d(TAG, "Execute NEXT command")
            }
            RemoteCommandType.PREVIOUS -> {
                // TODO: Call media player previous()
                Log.d(TAG, "Execute PREVIOUS command")
            }
            RemoteCommandType.SEEK -> {
                val position = command.parameter?.toLongOrNull() ?: 0
                // TODO: Call media player seekTo(position)
                Log.d(TAG, "Execute SEEK command: $position")
            }
            RemoteCommandType.VOLUME -> {
                val volume = command.parameter?.toIntOrNull() ?: 50
                // TODO: Set device volume
                Log.d(TAG, "Execute VOLUME command: $volume")
            }
            RemoteCommandType.PLAY_SONG -> {
                val songId = command.parameter ?: ""
                // TODO: Play specific song by ID
                Log.d(TAG, "Execute PLAY_SONG command: $songId")
            }
            RemoteCommandType.SHUFFLE -> {
                val enabled = command.parameter?.toBoolean() ?: false
                // TODO: Set shuffle mode
                Log.d(TAG, "Execute SHUFFLE command: $enabled")
            }
            RemoteCommandType.REPEAT -> {
                val mode = command.parameter ?: "OFF"
                // TODO: Set repeat mode
                Log.d(TAG, "Execute REPEAT command: $mode")
            }
            else -> {
                Log.w(TAG, "Unknown command: ${command.command}")
            }
        }
    }
    
    /**
     * Update current player state to remote
     */
    fun updatePlayerState(
        isPlaying: Boolean,
        currentSong: SongInfo?,
        position: Long,
        duration: Long,
        repeatMode: String,
        shuffleMode: Boolean,
        volume: Int
    ) {
        viewModelScope.launch {
            val state = DeviceState(
                isPlaying = isPlaying,
                currentSong = currentSong,
                position = position,
                duration = duration,
                repeatMode = repeatMode,
                shuffleMode = shuffleMode,
                volume = volume
            )
            remoteControlManager.updateDeviceState(state)
        }
    }
    
    /**
     * Update playlists for remote access
     */
    fun updatePlaylists(playlists: List<RemotePlaylist>) {
        viewModelScope.launch {
            remoteControlManager.updatePlaylists(playlists)
        }
    }
    
    /**
     * Disconnect remote control
     */
    fun disconnect() {
        _connectionState.value = ConnectionState.Disconnected
    }
}
