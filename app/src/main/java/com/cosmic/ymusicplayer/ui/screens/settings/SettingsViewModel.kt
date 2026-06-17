package com.cosmic.ymusicplayer.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosmic.ymusicplayer.data.repository.MusicRepository
import com.cosmic.ymusicplayer.util.ThemeManager
import com.cosmic.ymusicplayer.util.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themeManager: ThemeManager,
    private val musicRepository: MusicRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themeManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeManager.setThemeMode(mode)
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                musicRepository.scanMusic()
            } finally {
                _isScanning.value = false
            }
        }
    }

    class Factory(
        private val themeManager: ThemeManager,
        private val musicRepository: MusicRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(themeManager, musicRepository) as T
        }
    }
}
