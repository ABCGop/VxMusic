package com.abcg.music.data.model.explore.mood.moodmoments

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import com.abcg.music.data.model.searchResult.songs.Thumbnail
import com.abcg.music.data.type.HomeContentType

@Immutable
data class Content(
    @SerializedName("playlistBrowseId")
    val playlistBrowseId: String,
    @SerializedName("subtitle")
    val subtitle: String,
    @SerializedName("thumbnails")
    val thumbnails: List<Thumbnail>?,
    @SerializedName("title")
    val title: String,
) : HomeContentType