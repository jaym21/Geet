package dev.jaym21.geet.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import dev.jaym21.geet.models.QueueEntity

@Dao
interface QueueDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQueue(queue: QueueEntity)
}