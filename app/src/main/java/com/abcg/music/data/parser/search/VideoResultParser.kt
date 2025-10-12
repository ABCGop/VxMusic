package com.abcg.music.data.parser.search

import com.abcg.kotlinytmusicscraper.models.SongItem
import com.abcg.kotlinytmusicscraper.pages.SearchResult
import com.abcg.music.data.model.searchResult.songs.Artist
import com.abcg.music.data.model.searchResult.songs.Thumbnail
import com.abcg.music.data.model.searchResult.videos.VideosResult

fun parseSearchVideo(result: SearchResult): ArrayList<VideosResult> {
    val songsResult: ArrayList<VideosResult> = arrayListOf()
    result.items.forEach {
        val song = it as SongItem
        songsResult.add(
            VideosResult(
                artists =
                    song.artists.map { artistItem ->
                        Artist(
                            id = artistItem.id,
                            name = artistItem.name,
                        )
                    },
                category = "Video",
                duration = if (song.duration != null) "%02d:%02d".format(song.duration!! / 60, song.duration!! % 60) else "",
                durationSeconds = song.duration ?: 0,
                resultType = "Video",
                thumbnails = listOf(Thumbnail(306, Regex("([wh])120").replace(song.thumbnail, "$1544"), 544)),
                title = song.title,
                videoId = song.id,
                videoType = "Video",
                views = null,
                year = "",
            ),
        )
    }
    return songsResult
}