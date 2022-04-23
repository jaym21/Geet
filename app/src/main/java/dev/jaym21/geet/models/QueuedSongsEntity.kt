package dev.jaym21.geet.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queued_songs")
data class QueuedSongsEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var songId: Long
)