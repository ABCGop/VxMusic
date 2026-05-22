package com.maxrave.domain.data.type

data class ChartItem(
    val country: Country,
    val ytPlaylistId: String,
) : PlaylistType {
    override fun playlistType(): PlaylistType.Type = PlaylistType.Type.YOUTUBE_PLAYLIST

    enum class Country {
        GLOBAL,
        VIETNAM,
        ITALY,
        INDIA,
        INDONESIA,
        BRAZIL,
        MEXICO,
        UNITED_STATE,
    }
}