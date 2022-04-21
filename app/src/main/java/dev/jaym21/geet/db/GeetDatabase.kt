package dev.jaym21.geet.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.jaym21.geet.models.QueueEntity
import dev.jaym21.geet.models.QueuedSongsEntity

@Database(entities = [QueueEntity::class, QueuedSongsEntity::class], version = 1, exportSchema = false)
abstract class GeetDatabase: RoomDatabase() {

    abstract fun queueDao: QueueDAO()
}