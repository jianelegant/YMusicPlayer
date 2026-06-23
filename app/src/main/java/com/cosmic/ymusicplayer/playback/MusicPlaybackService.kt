package com.cosmic.ymusicplayer.playback

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager
import com.cosmic.ymusicplayer.MainActivity
import com.cosmic.ymusicplayer.R
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicPlaybackService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var musicPlayer: MusicPlayer
    private var positionUpdateJob: Job? = null

    private var mediaSession: MediaSession? = null
    private var playerNotificationManager: PlayerNotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        musicPlayer = MusicPlayer(this)

        // Build MediaSession
        mediaSession = MediaSession.Builder(this, musicPlayer.player)
            .setCallback(object : MediaSession.Callback {
                override fun onAddMediaItems(
                    mediaSession: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    mediaItems: List<MediaItem>
                ): ListenableFuture<List<MediaItem>> {
                    return Futures.immediateFuture(emptyList())
                }
            })
            .build()

        // Build notification manager
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            MusicPlayer.NOTIFICATION_ID,
            MusicPlayer.CHANNEL_ID
        )
            .setChannelNameResourceId(R.string.notification_channel_name)
            .setChannelDescriptionResourceId(R.string.notification_channel_desc)
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return musicPlayer.getCurrentSong()?.title ?: "Not Playing"
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    return pendingIntent
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return musicPlayer.getCurrentSong()?.artist
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    return null
                }
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            })
            .build()

        playerNotificationManager?.setPlayer(musicPlayer.player)
        playerNotificationManager?.setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
        playerNotificationManager?.setUseNextAction(true)
        playerNotificationManager?.setUsePreviousAction(true)
        playerNotificationManager?.setUseStopAction(true)

        // Register headphone receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction("android.media.AUDIO_BECOMING_NOISY")
        }
        registerReceiver(headphoneReceiver, filter)

        // Observe player state for position updates
        serviceScope.launch {
            musicPlayer.playerState.collect { state ->
                if (state.isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }
        }

        _player.value = musicPlayer
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PLAY" -> musicPlayer.play()
            "PAUSE" -> musicPlayer.pause()
            "NEXT" -> musicPlayer.skipToNext()
            "PREVIOUS" -> musicPlayer.skipToPrevious()
            "CLOSE" -> {
                musicPlayer.pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                playerNotificationManager?.setPlayer(null)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        unregisterReceiver(headphoneReceiver)
        playerNotificationManager?.setPlayer(null)
        playerNotificationManager = null
        mediaSession?.release()
        mediaSession = null
        musicPlayer.release()
        _player.value = null
        super.onDestroy()
    }

    fun getMusicPlayer(): MusicPlayer = musicPlayer

    private fun startPositionUpdates() {
        if (positionUpdateJob?.isActive == true) return
        positionUpdateJob = serviceScope.launch {
            while (isActive) {
                musicPlayer.updatePosition()
                delay(250)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private val headphoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)
                    if (state == 0) {
                        musicPlayer.pause()
                    }
                }
                "android.media.AUDIO_BECOMING_NOISY" -> {
                    musicPlayer.pause()
                }
            }
        }
    }

    companion object {
        private val _player = MutableStateFlow<MusicPlayer?>(null)
        val player: StateFlow<MusicPlayer?> = _player.asStateFlow()

        fun getPlayer(): MusicPlayer? = _player.value
    }
}
