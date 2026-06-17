package com.cosmic.ymusicplayer.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosmic.ymusicplayer.data.model.Song
import com.cosmic.ymusicplayer.data.repository.MusicRepository
import com.cosmic.ymusicplayer.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    val songs: StateFlow<List<Song>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) musicRepository.getAllSongs()
            else musicRepository.searchSongs(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun scanMusic() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                musicRepository.scanMusic()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song)
        }
    }

    fun addToHistory(songId: Long) {
        viewModelScope.launch {
            musicRepository.addToHistory(songId)
        }
    }

    class Factory(
        private val musicRepository: MusicRepository,
        private val playlistRepository: PlaylistRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(musicRepository, playlistRepository) as T
        }
    }
}
