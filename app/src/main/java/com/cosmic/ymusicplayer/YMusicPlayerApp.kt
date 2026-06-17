package com.cosmic.ymusicplayer

import android.app.Application
import android.content.Intent
import com.cosmic.ymusicplayer.data.local.MusicDatabase
import com.cosmic.ymusicplayer.data.local.MusicScanner
import com.cosmic.ymusicplayer.data.repository.MusicRepository
import com.cosmic.ymusicplayer.data.repository.PlaylistRepository
import com.cosmic.ymusicplayer.playback.MusicPlaybackService
import com.cosmic.ymusicplayer.playback.MusicPlayer
import com.cosmic.ymusicplayer.util.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class YMusicPlayerApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Database
    val database by lazy { MusicDatabase.getInstance(this) }

    // DAOs
    val songDao by lazy { database.songDao() }
    val playlistDao by lazy { database.playlistDao() }
    val playHistoryDao by lazy { database.playHistoryDao() }

    // Scanner
    val musicScanner by lazy { MusicScanner(this, songDao) }

    // Repositories
    val musicRepository by lazy { MusicRepository(songDao, playHistoryDao, musicScanner) }
    val playlistRepository by lazy { PlaylistRepository(playlistDao) }

    // Theme
    val themeManager by lazy { ThemeManager(this) }

    // Player — delegates to the service instance
    val musicPlayer: MusicPlayer
        get() = MusicPlaybackService.getPlayer()
            ?: throw IllegalStateException("MusicPlaybackService not started")

    fun startMusicService() {
        val intent = Intent(this, MusicPlaybackService::class.java)
        startService(intent)

        // Initial scan
        applicationScope.launch {
            musicScanner.scan()
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: YMusicPlayerApp
            private set
    }
}
