package com.cosmic.ymusicplayer.playback

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.cosmic.ymusicplayer.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicPlayer(
    private val context: Context
) : Player.Listener {

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var playlist: List<Song> = emptyList()
    private var currentIndex: Int = -1
    private var isUserSeeking = false

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "playback_channel"
    }

    init {
        player.addListener(this)
    }

    // --- Playback Control ---

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        playlist = songs
        currentIndex = startIndex.coerceIn(0, songs.lastIndex)
        player.setMediaItems(songs.map { it.toMediaItem() }, currentIndex, 0)
        player.prepare()
    }

    fun play() {
        if (player.playbackState == Player.STATE_IDLE && playlist.isNotEmpty()) {
            player.prepare()
        }
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun togglePlayPause() {
        if (player.isPlaying) pause() else play()
    }

    fun seekTo(positionMs: Long) {
        isUserSeeking = true
        player.seekTo(positionMs)
    }

    fun playAtIndex(index: Int) {
        if (index in playlist.indices) {
            currentIndex = index
            player.seekToDefaultPosition(index)
            player.play()
        }
    }

    fun skipToNext() {
        if (playlist.isEmpty()) return

        val nextIndex = when (_playerState.value.repeatMode) {
            RepeatMode.ONE -> currentIndex
            RepeatMode.OFF, RepeatMode.ALL -> {
                if (_playerState.value.isShuffle) {
                    (playlist.indices).random()
                } else {
                    if (currentIndex < playlist.lastIndex) currentIndex + 1 else {
                        if (_playerState.value.repeatMode == RepeatMode.ALL) 0 else return
                    }
                }
            }
        }
        playAtIndex(nextIndex)
    }

    fun skipToPrevious() {
        if (playlist.isEmpty()) return
        if (player.currentPosition > 3000) {
            player.seekTo(0)
        } else {
            val prevIndex = if (currentIndex > 0) currentIndex - 1
            else if (_playerState.value.repeatMode == RepeatMode.ALL) playlist.lastIndex
            else return
            playAtIndex(prevIndex)
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        _playerState.value = _playerState.value.copy(repeatMode = mode)
    }

    fun toggleShuffle() {
        _playerState.value = _playerState.value.copy(isShuffle = !_playerState.value.isShuffle)
    }

    fun getCurrentSong(): Song? = playlist.getOrNull(currentIndex)

    // --- Player.Listener ---

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val index = player.currentMediaItemIndex
        if (index in playlist.indices) {
            currentIndex = index
            _playerState.value = _playerState.value.copy(
                currentSong = playlist[index],
                currentIndex = index
            )
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) {
            if (_playerState.value.repeatMode == RepeatMode.ONE) {
                player.seekTo(0)
                player.play()
            } else {
                skipToNext()
            }
        }
        _playerState.value = _playerState.value.copy(
            duration = if (player.duration > 0) player.duration else _playerState.value.duration
        )
    }

    fun updatePosition() {
        if (!isUserSeeking) {
            _playerState.value = _playerState.value.copy(
                currentPosition = player.currentPosition,
                duration = if (player.duration > 0) player.duration else _playerState.value.duration
            )
        }
    }

    fun onSeekFinished() {
        isUserSeeking = false
    }

    fun release() {
        player.release()
    }
}

@OptIn(UnstableApi::class)
private fun Song.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(Uri.parse(uri))
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(albumArtUri?.let { Uri.parse(it) })
                .build()
        )
        .build()
}
