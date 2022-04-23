package dev.jaym21.geet.repository

import dev.jaym21.geet.db.QueueDAO
import dev.jaym21.geet.extensions.equalsBy
import dev.jaym21.geet.extensions.toQueuedSongsList
import dev.jaym21.geet.models.QueueEntity

class QueueRepository(private val queueDAO: QueueDAO, private val songsRepository: SongsRepository) {

    fun updateQueue(queue: QueueEntity) = queueDAO.insertQueue(queue)

    private fun setCurrentSongId(id: Long) = queueDAO.setCurrentId(id)

    fun updateQueuedSongs(queueIds: LongArray?, currentSongId: Long?) {
        if (queueIds == null || currentSongId == null)
            return

        val currentList = queueDAO.getQueuedSongs()
        val songsList = queueIds.toQueuedSongsList(songsRepository)

        val isListEqual = currentList.equalsBy(songsList) { left, right ->
            left.id == right.id
        }

        if (queueIds.isNotEmpty() && !isListEqual) {
            queueDAO.clearQueueSongs()
            queueDAO.insertAllSongs(songsList)
            setCurrentSongId(currentSongId)
        } else {
            setCurrentSongId(currentSongId)
        }
    }
}