package com.abcg.music.data.model.searchResult.artists

import com.abcg.music.data.model.searchResult.songs.Thumbnail
import com.abcg.music.data.type.ArtistType

data class ArtistsResult(
    val artist: String,
    val browseId: String,
    val category: String,
    val radioId: String,
    val resultType: String,
    val shuffleId: String,
    val thumbnails: List<Thumbnail>,
) : ArtistType