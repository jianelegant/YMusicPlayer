package com.cosmic.ymusicplayer.ui.screens.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosmic.ymusicplayer.data.model.Playlist
import com.cosmic.ymusicplayer.data.model.Song
import com.cosmic.ymusicplayer.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    val playlistSongs: StateFlow<List<Song>> = _selectedPlaylistId
        .flatMapLatest { id ->
            if (id != null) playlistRepository.getSongsInPlaylist(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlaylistName = MutableStateFlow("")
    val selectedPlaylistName: StateFlow<String> = _selectedPlaylistName.asStateFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _showAddToPlaylistDialog = MutableStateFlow<Song?>(null)
    val showAddToPlaylistDialog: StateFlow<Song?> = _showAddToPlaylistDialog.asStateFlow()

    fun selectPlaylist(playlist: Playlist) {
        _selectedPlaylistId.value = playlist.id
        _selectedPlaylistName.value = playlist.name
    }

    fun clearSelection() {
        _selectedPlaylistId.value = null
        _selectedPlaylistName.value = ""
    }

    fun showCreateDialog() { _showCreateDialog.value = true }
    fun hideCreateDialog() { _showCreateDialog.value = false }

    fun showAddToPlaylistDialog(song: Song) { _showAddToPlaylistDialog.value = song }
    fun hideAddToPlaylistDialog() { _showAddToPlaylistDialog.value = null }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name)
            _showCreateDialog.value = false
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
            _selectedPlaylistId.value = null
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun addToPlaylistAndDismiss(playlistId: Long, song: Song) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, song.id)
            _showAddToPlaylistDialog.value = null
        }
    }

    class Factory(
        private val playlistRepository: PlaylistRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaylistViewModel(playlistRepository) as T
        }
    }
}
