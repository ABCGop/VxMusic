package com.abcg.music.data.model.searchResult.albums

import com.abcg.music.data.model.searchResult.songs.Artist
import com.abcg.music.data.model.searchResult.songs.Thumbnail
import com.abcg.music.data.type.PlaylistType

data class AlbumsResult(
    val artists: List<Artist>,
    val browseId: String,
    val category: String,
    val duration: Any,
    val isExplicit: Boolean,
    val resultType: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val type: String,
    val year: String,
) : PlaylistType {
    override fun playlistType(): PlaylistType.Type = PlaylistType.Type.ALBUM
}