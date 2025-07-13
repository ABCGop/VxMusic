package com.abcg.music.data.model.searchResult.artists

import com.google.gson.annotations.SerializedName
import com.abcg.music.data.model.searchResult.songs.Thumbnail
import com.abcg.music.data.type.ArtistType

data class ArtistsResult(
    @SerializedName("artist")
    val artist: String,
    @SerializedName("browseId")
    val browseId: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("radioId")
    val radioId: String,
    @SerializedName("resultType")
    val resultType: String,
    @SerializedName("shuffleId")
    val shuffleId: String,
    @SerializedName("thumbnails")
    val thumbnails: List<Thumbnail>,
): ArtistType