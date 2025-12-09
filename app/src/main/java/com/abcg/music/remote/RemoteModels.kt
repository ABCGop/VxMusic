package com.abcg.music.remote

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Remote Control Command Model
 * Represents a command sent from the web/remote interface to control the app
 */
@IgnoreExtraProperties
data class RemoteCommand(
    val commandId: String = "",
    val command: String = "",
    val parameter: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val executed: Boolean = false
)

/**
 * Device State Model
 * Current state of the music player
 */
@IgnoreExtraProperties
data class DeviceState(
    val deviceId: String = "",
    val deviceName: String = "",
    val isPlaying: Boolean = false,
    val currentSong: SongInfo? = null,
    val volume: Int = 50,
    val position: Long = 0,
    val duration: Long = 0,
    val repeatMode: String = "OFF", // OFF, ONE, ALL
    val shuffleMode: Boolean = false,
    val lastUpdate: Long = System.currentTimeMillis()
)

/**
 * Song Information Model
 */
@IgnoreExtraProperties
data class SongInfo(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val artworkUrl: String = "",
    val duration: Long = 0
)

/**
 * Playlist Model
 */
@IgnoreExtraProperties
data class RemotePlaylist(
    val id: String = "",
    val name: String = "",
    val songs: List<SongInfo> = emptyList(),
    val artworkUrl: String = ""
)

/**
 * Command Types
 */
object RemoteCommandType {
    const val PLAY = "play"
    const val PAUSE = "pause"
    const val NEXT = "next"
    const val PREVIOUS = "previous"
    const val SEEK = "seek"
    const val VOLUME = "volume"
    const val PLAY_SONG = "play_song"
    const val PLAY_PLAYLIST = "play_playlist"
    const val SHUFFLE = "shuffle"
    const val REPEAT = "repeat"
    const val SEARCH = "search"
    const val ADD_TO_QUEUE = "add_to_queue"
    const val CLEAR_QUEUE = "clear_queue"
}
