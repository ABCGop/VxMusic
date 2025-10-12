package com.abcg.music.data.model.explore.mood.moodmoments

import androidx.compose.runtime.Immutable
import com.abcg.music.data.model.searchResult.songs.Thumbnail
import com.abcg.music.data.type.HomeContentType

@Immutable
data class Content(
    val playlistBrowseId: String,
    val subtitle: String,
    val thumbnails: List<Thumbnail>?,
    val title: String,
) : HomeContentType