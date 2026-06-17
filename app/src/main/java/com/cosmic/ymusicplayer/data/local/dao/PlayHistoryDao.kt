package com.cosmic.ymusicplayer.data.local.dao

import androidx.room.*
import com.cosmic.ymusicplayer.data.model.PlayHistory
import com.cosmic.ymusicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {

    @Query("SELECT s.* FROM songs s INNER JOIN play_history ph ON s.id = ph.songId ORDER BY ph.playedAt DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PlayHistory)

    @Query("DELETE FROM play_history WHERE songId NOT IN (SELECT songId FROM play_history ORDER BY playedAt DESC LIMIT 20)")
    suspend fun trimToMaxEntries()
}
