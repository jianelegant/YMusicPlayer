package com.cosmic.ymusicplayer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cosmic.ymusicplayer.data.local.dao.PlayHistoryDao
import com.cosmic.ymusicplayer.data.local.dao.PlaylistDao
import com.cosmic.ymusicplayer.data.local.dao.SongDao
import com.cosmic.ymusicplayer.data.model.PlayHistory
import com.cosmic.ymusicplayer.data.model.Playlist
import com.cosmic.ymusicplayer.data.model.PlaylistSong
import com.cosmic.ymusicplayer.data.model.Song

@Database(
    entities = [Song::class, Playlist::class, PlaylistSong::class, PlayHistory::class],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playHistoryDao(): PlayHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "ymusic_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
