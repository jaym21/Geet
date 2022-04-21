package dev.jaym21.geet.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_table")
data class QueueEntity (
    @PrimaryKey(autoGenerate = false)
    var id: Long = 0,
    var queueTitle: String = "All Songs",
    var currentId: Long? = 0,
    var currentSeekPosition: Long? = 0,
    var repeatMode: Int? = 0,
    var shuffleMode: Int? = 0,
    var playState: Int? = 0
) {
    constructor(): this(0, "", 0, 0, 0, 0, 0)
}