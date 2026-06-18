package com.cosmic.ymusicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmic.ymusicplayer.domain.model.Song
import com.cosmic.ymusicplayer.playback.MusicController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val musicController: MusicController
) : ViewModel() {

    val currentSong = musicController.currentSong
    val isPlaying = musicController.isPlaying
    val currentPosition = musicController.currentPosition
    val duration = musicController.duration
    val shuffleModeEnabled = musicController.shuffleModeEnabled
    val repeatMode = musicController.repeatMode
    val playbackState = musicController.playbackState

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        musicController.play(songs, startIndex)
    }

    fun pauseResume() {
        if (isPlaying.value) {
            musicController.pause()
        } else {
            musicController.resume()
        }
    }

    fun skipToNext() = musicController.skipToNext()
    fun skipToPrevious() = musicController.skipToPrevious()
    fun seekTo(position: Long) = musicController.seekTo(position)
    fun toggleShuffle() = musicController.toggleShuffle()
    fun setRepeatMode(mode: Int) = musicController.setRepeatMode(mode)

    override fun onCleared() {
        super.onCleared()
        musicController.release()
    }
}
