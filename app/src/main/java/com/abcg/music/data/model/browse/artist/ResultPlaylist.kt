package com.abcg.music.data.model.browse.artist

import com.abcg.music.data.model.searchResult.songs.Thumbnail
import com.abcg.music.data.type.HomeContentType

data class ResultPlaylist(
    val id: String,
    val author: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
) : HomeContentType