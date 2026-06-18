package com.cosmic.ymusicplayer.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.cosmic.ymusicplayer.data.local.dao.MusicDao
import com.cosmic.ymusicplayer.data.local.entity.*
import com.cosmic.ymusicplayer.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

class MusicRepository(
    private val context: Context,
    private val musicDao: MusicDao
) {
    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun fetchLocalSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown"
                val album = cursor.getString(albumColumn) ?: "Unknown"
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(dataColumn)
                val albumId = cursor.getLong(albumIdColumn)

                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val albumArtUri = ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),
                    albumId
                )

                songs.add(Song(id, title, artist, album, duration, uri, albumArtUri, path))
            }
        }
        songs
    }

    // Playlist operations
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()
    
    suspend fun createPlaylist(name: String) = musicDao.insertPlaylist(PlaylistEntity(name = name))
    
    suspend fun deletePlaylist(playlist: PlaylistEntity) = musicDao.deletePlaylist(playlist)
    
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = 
        musicDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = 
        musicDao.removeSongFromPlaylist(playlistId, songId)
    
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Long>> = musicDao.getSongsInPlaylist(playlistId)

    // Favorites
    fun getFavorites(): Flow<List<FavoriteEntity>> = musicDao.getFavorites()
    
    suspend fun toggleFavorite(songId: Long) {
        val isFav = musicDao.isFavorite(songId).first()
        if (isFav) {
            musicDao.removeFavorite(songId)
        } else {
            musicDao.addFavorite(FavoriteEntity(songId))
        }
    }
    
    fun isFavorite(songId: Long): Flow<Boolean> = musicDao.isFavorite(songId)

    // History
    fun getRecentHistory(): Flow<List<HistoryEntity>> = musicDao.getRecentHistory()
    
    suspend fun addToHistory(songId: Long) = musicDao.addToHistory(HistoryEntity(songId))
}
