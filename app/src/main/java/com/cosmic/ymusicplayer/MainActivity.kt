package com.cosmic.ymusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.cosmic.ymusicplayer.data.local.AppDatabase
import com.cosmic.ymusicplayer.data.repository.MusicRepository
import com.cosmic.ymusicplayer.playback.MusicController
import com.cosmic.ymusicplayer.ui.screen.MainScreen
import com.cosmic.ymusicplayer.ui.theme.YMusicPlayerTheme
import com.cosmic.ymusicplayer.ui.viewmodel.MainViewModel
import com.cosmic.ymusicplayer.ui.viewmodel.MusicViewModel
import com.cosmic.ymusicplayer.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var musicViewModel: MusicViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            musicViewModel.loadSongs()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(this)
        val repository = MusicRepository(this, database.musicDao)
        val musicController = MusicController(this)
        val factory = ViewModelFactory(repository, musicController)

        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
        musicViewModel = ViewModelProvider(this, factory)[MusicViewModel::class.java]

        checkAndRequestPermissions()

        setContent {
            YMusicPlayerTheme {
                MainScreen(
                    mainViewModel = mainViewModel,
                    musicViewModel = musicViewModel
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                musicViewModel.loadSongs()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}
