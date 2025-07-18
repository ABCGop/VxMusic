package com.abcg.music.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.abcg.music.data.db.entities.AlbumEntity
import com.abcg.music.data.db.entities.ArtistEntity
import com.abcg.music.data.db.entities.FollowedArtistSingleAndAlbum
import com.abcg.music.data.db.entities.GoogleAccountEntity
import com.abcg.music.data.db.entities.LocalPlaylistEntity
import com.abcg.music.data.db.entities.LyricsEntity
import com.abcg.music.data.db.entities.NewFormatEntity
import com.abcg.music.data.db.entities.NotificationEntity
import com.abcg.music.data.db.entities.PairSongLocalPlaylist
import com.abcg.music.data.db.entities.PlaylistEntity
import com.abcg.music.data.db.entities.QueueEntity
import com.abcg.music.data.db.entities.SearchHistory
import com.abcg.music.data.db.entities.SetVideoIdEntity
import com.abcg.music.data.db.entities.SongEntity
import com.abcg.music.data.db.entities.SongInfoEntity
import com.abcg.music.data.db.entities.TranslatedLyricsEntity

@Database(
    entities = [
        NewFormatEntity::class, SongInfoEntity::class, SearchHistory::class, SongEntity::class, ArtistEntity::class,
        AlbumEntity::class, PlaylistEntity::class, LocalPlaylistEntity::class, LyricsEntity::class, QueueEntity::class,
        SetVideoIdEntity::class, PairSongLocalPlaylist::class, GoogleAccountEntity::class, FollowedArtistSingleAndAlbum::class,
        NotificationEntity::class, TranslatedLyricsEntity::class
    ],
    version = 15,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3), AutoMigration(
            from = 1,
            to = 3,
        ), AutoMigration(from = 3, to = 4), AutoMigration(from = 2, to = 4), AutoMigration(
            from = 3,
            to = 5,
        ), AutoMigration(4, 5), AutoMigration(6, 7), AutoMigration(
            7,
            8,
            spec = AutoMigration7_8::class,
        ), AutoMigration(8, 9),
        AutoMigration(9, 10),
        AutoMigration(from = 11, to = 12, spec = AutoMigration11_12::class),
        AutoMigration(13, 14),
        AutoMigration(14, 15),
    ],
)
@TypeConverters(Converters::class)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun getDatabaseDao(): DatabaseDao
}