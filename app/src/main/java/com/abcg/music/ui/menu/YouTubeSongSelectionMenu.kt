package com.abcg.music.ui.menu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.abcg.innertube.models.SongItem
import com.abcg.music.LocalDatabase
import com.abcg.music.LocalPlayerConnection
import com.abcg.music.R
import com.abcg.music.extensions.toMediaItem
import com.abcg.music.models.toMediaMetadata
import com.abcg.music.playback.queues.ListQueue
import com.abcg.music.ui.component.GridMenu
import com.abcg.music.ui.component.GridMenuItem
import java.time.LocalDateTime

@Composable
fun YouTubeSongSelectionMenu(
    selection: List<SongItem>,
    onDismiss: () -> Unit,
    onExitSelectionMode: () -> Unit,
) {
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddToPlaylistDialog(
        isVisible = showChoosePlaylistDialog,
        onGetSong = {
            val mediaMetadatas = selection.map {
                it.toMediaMetadata()
            }
            database.transaction {
                mediaMetadatas.forEach(::insert)
            }
            selection.map { it.id }
        },
        onDismiss = { showChoosePlaylistDialog = false },
    )

    GridMenu(
        contentPadding =
        PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        ),
    ) {
        GridMenuItem(
            icon = R.drawable.play,
            title = R.string.play,
        ) {
            onDismiss()
            playerConnection.playQueue(
                ListQueue(
                    items = selection.map { it.toMediaItem() },
                ),
            )
            onExitSelectionMode()
        }

        GridMenuItem(
            icon = R.drawable.shuffle,
            title = R.string.shuffle,
        ) {
            onDismiss()
            playerConnection.playQueue(
                ListQueue(
                    items = selection.shuffled().map { it.toMediaItem() },
                ),
            )
            onExitSelectionMode()
        }

        GridMenuItem(
            icon = R.drawable.playlist_play,
            title = R.string.play_next,
        ) {
            onDismiss()
            playerConnection.playNext(selection.map { it.toMediaItem() })
            onExitSelectionMode()
        }

        GridMenuItem(
            icon = R.drawable.queue_music,
            title = R.string.add_to_queue,
        ) {
            onDismiss()
            playerConnection.addToQueue(selection.map { it.toMediaItem() })
            onExitSelectionMode()
        }

        GridMenuItem(
            icon = R.drawable.playlist_add,
            title = R.string.add_to_playlist,
        ) {
            showChoosePlaylistDialog = true
        }

        GridMenuItem(
            icon = R.drawable.library_add,
            title = R.string.add_to_library,
        ) {
            database.query {
                selection.forEach { song ->
                    insert(song.toMediaMetadata())
                }
                selection.forEach { song ->
                    inLibrary(song.id, LocalDateTime.now())
                }
            }
            onDismiss()
        }
    }
}
