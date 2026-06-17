package com.cosmic.ymusicplayer.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "play_history",
    primaryKeys = ["songId"],
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playedAt"])]
)
data class PlayHistory(
    val songId: Long,
    val playedAt: Long = System.currentTimeMillis()
)
