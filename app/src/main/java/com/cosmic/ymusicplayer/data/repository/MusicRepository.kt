package com.cosmic.ymusicplayer.data.repository

import com.cosmic.ymusicplayer.data.local.MusicScanner
import com.cosmic.ymusicplayer.data.local.dao.PlayHistoryDao
import com.cosmic.ymusicplayer.data.local.dao.SongDao
import com.cosmic.ymusicplayer.data.model.PlayHistory
import com.cosmic.ymusicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val songDao: SongDao,
    private val playHistoryDao: PlayHistoryDao,
    private val musicScanner: MusicScanner
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()

    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)

    fun getFavorites(): Flow<List<Song>> = songDao.getFavorites()

    fun getRecentHistory(): Flow<List<Song>> = playHistoryDao.getRecentHistory()

    suspend fun getSongById(id: Long): Song? = songDao.getSongById(id)

    suspend fun toggleFavorite(song: Song) {
        songDao.setFavorite(song.id, !song.isFavorite)
    }

    suspend fun addToHistory(songId: Long) {
        playHistoryDao.insert(PlayHistory(songId = songId))
        playHistoryDao.trimToMaxEntries()
    }

    suspend fun scanMusic(): List<Song> = musicScanner.scan()
}
