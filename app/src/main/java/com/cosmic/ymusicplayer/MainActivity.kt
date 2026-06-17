package com.cosmic.ymusicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.cosmic.ymusicplayer.playback.MusicPlaybackService
import com.cosmic.ymusicplayer.ui.navigation.AppNavigation
import com.cosmic.ymusicplayer.ui.theme.YMusicTheme
import com.cosmic.ymusicplayer.util.ThemeMode

class MainActivity : ComponentActivity() {

    private val app: YMusicPlayerApp by lazy { application as YMusicPlayerApp }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission granted, start scanning
            app.startMusicService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check and request permissions
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            app.startMusicService()
            setupUI()
        } else {
            permissionLauncher.launch(permission)
            // Still set up UI so user can see the app
            setupUI()
        }
    }

    private fun setupUI() {
        setContent {
            val themeMode by app.themeManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            YMusicTheme(themeMode = themeMode) {
                AppNavigation(app = app)
            }
        }
    }
}
