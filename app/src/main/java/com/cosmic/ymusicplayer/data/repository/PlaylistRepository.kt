package com.cosmic.ymusicplayer.data.repository

import com.cosmic.ymusicplayer.data.local.dao.PlaylistDao
import com.cosmic.ymusicplayer.data.model.Playlist
import com.cosmic.ymusicplayer.data.model.PlaylistSong
import com.cosmic.ymusicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(
    private val playlistDao: PlaylistDao
) {
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> =
        playlistDao.getSongsInPlaylist(playlistId)

    fun getPlaylistsWithoutSong(songId: Long): Flow<List<Playlist>> =
        playlistDao.getPlaylistsWithoutSong(songId)

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(Playlist(name = name))
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val maxPos = playlistDao.getMaxPosition(playlistId) ?: -1
        playlistDao.addSongToPlaylist(
            PlaylistSong(playlistId = playlistId, songId = songId, position = maxPos + 1)
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun getPlaylistById(id: Long): Playlist? = playlistDao.getPlaylistById(id)
}
