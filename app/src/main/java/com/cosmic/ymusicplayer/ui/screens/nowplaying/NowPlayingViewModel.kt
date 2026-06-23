package com.cosmic.ymusicplayer.ui.screens.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosmic.ymusicplayer.data.model.Song
import com.cosmic.ymusicplayer.data.repository.MusicRepository
import com.cosmic.ymusicplayer.playback.MusicPlayer
import com.cosmic.ymusicplayer.playback.PlayerState
import com.cosmic.ymusicplayer.playback.RepeatMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NowPlayingViewModel(
    private val musicPlayerFlow: StateFlow<MusicPlayer?>,
    private val musicRepository: MusicRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = musicPlayerFlow
        .flatMapLatest { player ->
            player?.playerState ?: flowOf(PlayerState())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerState())

    // Position updates tick (mirrors the service loop for UI smoothness)
    init {
        viewModelScope.launch {
            while (isActive) {
                if (playerState.value.isPlaying) {
                    musicPlayerFlow.value?.updatePosition()
                }
                kotlinx.coroutines.delay(250)
            }
        }
    }

    fun togglePlayPause() {
        musicPlayerFlow.value?.togglePlayPause()
    }

    fun skipToNext() {
        musicPlayerFlow.value?.skipToNext()
    }

    fun skipToPrevious() {
        musicPlayerFlow.value?.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        musicPlayerFlow.value?.seekTo(positionMs)
    }

    fun onSeekFinished() {
        musicPlayerFlow.value?.onSeekFinished()
    }

    fun cycleRepeatMode() {
        val player = musicPlayerFlow.value ?: return
        val next = when (playerState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        player.setRepeatMode(next)
    }

    fun toggleShuffle() {
        musicPlayerFlow.value?.toggleShuffle()
    }

    fun playSong(songs: List<Song>, index: Int) {
        musicPlayerFlow.value?.let { player ->
            player.setPlaylist(songs, index)
            player.play()
        }
        viewModelScope.launch {
            songs.getOrNull(index)?.let { musicRepository.addToHistory(it.id) }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            musicPlayerFlow.value?.getCurrentSong()?.let { musicRepository.toggleFavorite(it) }
        }
    }

    class Factory(
        private val musicPlayerFlow: StateFlow<MusicPlayer?>,
        private val musicRepository: MusicRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NowPlayingViewModel(musicPlayerFlow, musicRepository) as T
        }
    }
}
