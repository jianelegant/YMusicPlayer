package com.cosmic.ymusicplayer.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.cosmic.ymusicplayer.domain.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicController(context: Context) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _currentSong = MutableStateFlow<MediaItem?>(null)
    val currentSong: StateFlow<MediaItem?> = _currentSong.asStateFlow()

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled: StateFlow<Boolean> = _shuffleModeEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    _currentSong.value = mediaItem
                    _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    _playbackState.value = playbackState
                    if (playbackState == Player.STATE_READY) {
                        _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startProgressUpdate()
                    } else {
                        stopProgressUpdate()
                    }
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _shuffleModeEnabled.value = shuffleModeEnabled
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _repeatMode.value = repeatMode
                }
            })
            
            // Initial states
            _currentSong.value = mediaController?.currentMediaItem
            _isPlaying.value = mediaController?.isPlaying ?: false
            _playbackState.value = mediaController?.playbackState ?: Player.STATE_IDLE
            _shuffleModeEnabled.value = mediaController?.shuffleModeEnabled ?: false
            _repeatMode.value = mediaController?.repeatMode ?: Player.REPEAT_MODE_OFF
            _duration.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
            
        }, MoreExecutors.directExecutor())
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                _currentPosition.value = mediaController?.currentPosition ?: 0L
                delay(1000)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    fun play(songs: List<Song>, startIndex: Int = 0) {
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtUri)
                        .build()
                )
                .build()
        }
        mediaController?.setMediaItems(mediaItems, startIndex, 0L)
        mediaController?.prepare()
        mediaController?.play()
    }

    fun pause() = mediaController?.pause()
    fun resume() = mediaController?.play()
    fun seekTo(position: Long) = mediaController?.seekTo(position)
    fun skipToNext() = mediaController?.seekToNext()
    fun skipToPrevious() = mediaController?.seekToPrevious()
    
    fun toggleShuffle() {
        mediaController?.shuffleModeEnabled = !(mediaController?.shuffleModeEnabled ?: false)
    }

    fun setRepeatMode(mode: Int) {
        mediaController?.repeatMode = mode
    }

    fun release() {
        stopProgressUpdate()
        MediaController.releaseFuture(controllerFuture!!)
    }
}
