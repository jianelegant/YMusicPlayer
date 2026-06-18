package com.cosmic.ymusicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmic.ymusicplayer.data.local.entity.PlaylistEntity
import com.cosmic.ymusicplayer.data.repository.MusicRepository
import com.cosmic.ymusicplayer.domain.model.Song
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicViewModel(
    private val repository: MusicRepository
) : ViewModel() {

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSongs = combine(_allSongs, _searchQuery) { songs, query ->
        if (query.isBlank()) songs
        else songs.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.artist.contains(query, ignoreCase = true) 
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playlists = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favorites = repository.getFavorites()
    val history = repository.getRecentHistory()

    init {
        loadSongs()
    }

    fun loadSongs() {
        viewModelScope.launch {
            _allSongs.value = repository.fetchLocalSongs()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Playlist actions
    fun createPlaylist(name: String) = viewModelScope.launch {
        repository.createPlaylist(name)
    }

    fun deletePlaylist(playlist: PlaylistEntity) = viewModelScope.launch {
        repository.deletePlaylist(playlist)
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) = viewModelScope.launch {
        repository.addSongToPlaylist(playlistId, songId)
    }

    // Favorites
    fun toggleFavorite(songId: Long) = viewModelScope.launch {
        repository.toggleFavorite(songId)
    }
    
    fun isFavorite(songId: Long) = repository.isFavorite(songId)

    // History
    fun addToHistory(songId: Long) = viewModelScope.launch {
        repository.addToHistory(songId)
    }
}
