package com.cosmic.ymusicplayer.playback

enum class RepeatMode {
    OFF, ALL, ONE
}

data class PlayerState(
    val currentSong: com.cosmic.ymusicplayer.data.model.Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffle: Boolean = false,
    val queue: List<com.cosmic.ymusicplayer.data.model.Song> = emptyList(),
    val currentIndex: Int = -1
)
