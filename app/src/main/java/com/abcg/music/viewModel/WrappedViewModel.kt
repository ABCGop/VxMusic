package com.abcg.music.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.abcg.music.viewModel.base.BaseViewModel
import com.maxrave.domain.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class WrappedStats(
    val period: String, // e.g., "November 2025" or "2025"
    val topSong: String,
    val topSongArtist: String,
    val topSongPlays: Int,
    val topArtist: String,
    val topArtistPlays: Int,
    val topSongImage: String,
    val totalMinutes: Int
)

@androidx.annotation.Keep
class WrappedViewModel(
    application: Application,
    private val songRepository: SongRepository
) : BaseViewModel(application) {

    private val _wrappedStats = MutableStateFlow<WrappedStats?>(null)
    val wrappedStats: StateFlow<WrappedStats?> = _wrappedStats.asStateFlow()

    init {
        calculateWrappedStats()
    }

    private fun calculateWrappedStats() {
        viewModelScope.launch {
            try {
                // Collect real data from repository
                songRepository.getMostPlayedSongs().collect { songs ->
                    if (songs.isNotEmpty()) {
                        // 1. Total Minutes
                        val totalDurationSeconds = songs.sumOf { it.totalPlayTime } / 1000 // Accessing totalPlayTime
                        val totalMinutes = (totalDurationSeconds / 60).toInt()

                        // 2. Top Song
                        val topSongEntity = songs.first()
                        
                        // 3. Top Artist
                        val topArtistEntry = songs
                            .flatMap { it.artistName ?: emptyList() } // Flatten artist lists
                            .groupingBy { it }
                            .eachCount()
                            .maxByOrNull { it.value }
                        
                        val topArtistName = topArtistEntry?.key ?: "Unknown Artist"
                        val topArtistCount = topArtistEntry?.value ?: 0

                        val today = LocalDate.now()
                        val periodName = "${today.year} Wrapped" // Changed to Year for broader context

                        val realStats = WrappedStats(
                            period = periodName,
                            topSong = topSongEntity.title,
                            topSongArtist = topSongEntity.artistName?.firstOrNull() ?: "Unknown",
                            topSongPlays = (topSongEntity.totalPlayTime / (topSongEntity.durationSeconds * 1000L).coerceAtLeast(1)).toInt(), // Approx plays
                            topArtist = topArtistName,
                            topArtistPlays = topArtistCount * 5, // Rough estimate
                            topSongImage = topSongEntity.thumbnails ?: "",
                            totalMinutes = totalMinutes
                        )
                        _wrappedStats.value = realStats
                    } else {
                         // Fallback if no history
                          _wrappedStats.value = WrappedStats(
                            period = "Start Listening!",
                            topSong = "-",
                            topSongArtist = "-",
                            topSongPlays = 0,
                            topArtist = "-",
                            topArtistPlays = 0,

                            topSongImage = "",
                            totalMinutes = 0
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            try {
                // Take a snapshot of the current list to reset
                // We use firstOrNull() to get the current list without keeping the flow open
                val songs = songRepository.getMostPlayedSongs().firstOrNull() ?: emptyList()
                songs.forEach { song ->
                    songRepository.resetTotalPlayTime(song.videoId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
