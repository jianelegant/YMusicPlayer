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
    private val musicPlayer: MusicPlayer,
    private val musicRepository: MusicRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = musicPlayer.playerState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerState())

    // Position updates tick
    init {
        viewModelScope.launch {
            while (isActive) {
                if (playerState.value.isPlaying) {
                    musicPlayer.updatePosition()
                }
                kotlinx.coroutines.delay(250)
            }
        }
    }

    fun togglePlayPause() {
        musicPlayer.togglePlayPause()
    }

    fun skipToNext() {
        musicPlayer.skipToNext()
    }

    fun skipToPrevious() {
        musicPlayer.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        musicPlayer.seekTo(positionMs)
    }

    fun onSeekFinished() {
        musicPlayer.onSeekFinished()
    }

    fun cycleRepeatMode() {
        val next = when (playerState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        musicPlayer.setRepeatMode(next)
    }

    fun toggleShuffle() {
        musicPlayer.toggleShuffle()
    }

    fun playSong(songs: List<Song>, index: Int) {
        musicPlayer.setPlaylist(songs, index)
        musicPlayer.play()
        viewModelScope.launch {
            songs.getOrNull(index)?.let { musicRepository.addToHistory(it.id) }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            musicPlayer.getCurrentSong()?.let { musicRepository.toggleFavorite(it) }
        }
    }

    class Factory(
        private val musicPlayer: MusicPlayer,
        private val musicRepository: MusicRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NowPlayingViewModel(musicPlayer, musicRepository) as T
        }
    }
}
