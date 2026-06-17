package com.cosmic.ymusicplayer.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["title"]),
        Index(value = ["artist"])
    ]
)
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUri: String?,
    val duration: Long,
    val dateAdded: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
