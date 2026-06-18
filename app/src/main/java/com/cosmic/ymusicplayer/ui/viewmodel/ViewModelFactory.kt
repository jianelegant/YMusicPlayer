package com.cosmic.ymusicplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cosmic.ymusicplayer.data.repository.MusicRepository
import com.cosmic.ymusicplayer.playback.MusicController

class ViewModelFactory(
    private val repository: MusicRepository,
    private val musicController: MusicController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(musicController) as T
            }
            modelClass.isAssignableFrom(MusicViewModel::class.java) -> {
                MusicViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
