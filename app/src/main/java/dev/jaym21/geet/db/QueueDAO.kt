package dev.jaym21.geet.db

import androidx.room.*
import dev.jaym21.geet.models.QueueEntity
import dev.jaym21.geet.models.QueuedSongsEntity

@Dao
interface QueueDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQueue(queue: QueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSongs(songs: List<QueuedSongsEntity>)

    @Query("DELETE from queued_songs")
    fun clearQueueSongs()

    @Delete
    fun deleteSongFromQueue(song: QueuedSongsEntity)

    @Query("SELECT * FROM queue_table where id = 0")
    fun getQueue(): QueueEntity?

    @Query("SELECT * FROM queue_songs")
    fun getQueuedSongs(): List<QueuedSongsEntity>

    @Query("UPDATE queue_table SET currentId  = :currentId where id = 0")
    fun setCurrentId(currentId: Long)
}