package dev.jaym21.geet.db

import androidx.room.*
import dev.jaym21.geet.models.QueueEntity
import dev.jaym21.geet.models.QueuedSongsEntity

@Dao
interface QueueDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueue(queue: QueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSongs(songs: List<QueuedSongsEntity>)

    @Query("DELETE from queued_songs")
    suspend fun clearQueueSongs()

    @Delete
    suspend fun deleteSongFromQueue(song: QueuedSongsEntity)

    @Query("SELECT * FROM queue_table where id = 0")
    fun getQueue(): QueueEntity?

    @Query("SELECT * FROM queued_songs")
    fun getQueuedSongs(): List<QueuedSongsEntity>

    @Query("UPDATE queue_table SET currentId  = :currentId where id = 0")
    suspend fun setCurrentId(currentId: Long)
}