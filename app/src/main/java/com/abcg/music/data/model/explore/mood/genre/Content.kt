package com.abcg.music.data.model.explore.mood.genre

import androidx.compose.runtime.Immutable
import com.abcg.music.data.model.searchResult.songs.Thumbnail
import com.abcg.music.data.type.HomeContentType

@Immutable
data class Content(
    val playlistBrowseId: String,
    val thumbnail: List<Thumbnail>?,
    val title: Title,
) : HomeContentType