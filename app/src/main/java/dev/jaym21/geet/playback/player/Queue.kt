package dev.jaym21.geet.playback.player

import android.app.Application
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import dev.jaym21.geet.R
import dev.jaym21.geet.db.QueueDAO
import dev.jaym21.geet.extensions.moveElement
import dev.jaym21.geet.extensions.toQueue
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.repository.SongsRepository
import dev.jaym21.geet.utils.Constants
import java.util.*

//manage functions related to queue
class Queue(
    private val context: Application,
    private val songsRepository: SongsRepository,
    private val queueDao: QueueDAO
) {

    private lateinit var session: MediaSessionCompat
    private val shuffleRandom = Random()
    private val previousShuffles = mutableListOf<Int>()

     var ids = LongArray(0)
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                session.setQueue(queueItems())
            }
        }

     var title: String = context.getString(R.string.all_songs)
        set(value) {
            val previousValue = field
            field = if(value.isNotEmpty()) {
                value
            } else {
                context.getString(R.string.all_songs)
            }
            if (value != previousValue) {
                previousShuffles.clear()
                session.setQueueTitle(value)
            }
        }
    var currentSongId: Long = Constants.NO_SONG_ID

    val currentSongIndex: Int
        get() = ids.indexOf(currentSongId)

    val nextSongIndex: Int?
        get() {
            val nextIndex = currentSongIndex + 1
            val controller = session.controller
            return when {
                controller.shuffleMode == SHUFFLE_MODE_ALL -> getShuffleIndex()
                nextIndex < ids.size -> nextIndex
                else -> null
            }
        }

    val nextSongId: Long?
        get() {
            val nextIndex = nextSongIndex
            return if (nextIndex != null) {
                ids[nextIndex]
            } else {
                null
            }
        }

    val previousSongId: Long?
        get() {
            val previousIndex = currentSongIndex - 1
            return if (previousIndex >= 0) {
                ids[previousIndex]
            } else {
                null
            }
        }

    fun swap(from: Int, to: Int) {
        ids = ids.toMutableList()
            .moveElement(from, to)
            .toLongArray()
    }

    fun moveToNext(id: Long) {
        val nextIndex = currentSongIndex + 1
        val list = ids.toMutableList().apply {
            remove(id)
            add(nextIndex, id)
        }
        ids = list.toLongArray()
    }

    fun remove(id: Long) {
        val list = ids.toMutableList().apply {
            remove(id)
        }
        ids = list.toLongArray()
    }

    fun reset() {
        previousShuffles.clear()
        ids = LongArray(0)
        currentSongId = Constants.NO_SONG_ID
    }

    fun firstId(): Long {
        return ids.first()
    }

    fun lastId(): Long {
        return ids.last()
    }

    fun currentSong(): Song {
        return songsRepository.getSongForId(currentSongId)
    }

    fun confirmCurrentId() {
        if (currentSongId == Constants.NO_SONG_ID) {
            val queue = queueDao.getQueue()
            currentSongId = queue?.currentId ?: Constants.NO_SONG_ID
        }
    }

    fun queueItems(): List<MediaSessionCompat.QueueItem> {
        return ids.toQueue(songsRepository)
    }

    fun setMediaSession(session: MediaSessionCompat) {
        this.session = session
    }

    private fun getShuffleIndex(): Int {
        //getting a random new value within the max size of queue ids
        val newIndex = shuffleRandom.nextInt(ids.size - 1)
        //checking if the new index was received within 16 previous shuffles
        if (previousShuffles.contains(newIndex)) {
            //if its present then again fetching new index
            return getShuffleIndex()
        }
        //adding the new random index to previous shuffles
        previousShuffles.add(newIndex)
        //removing the first element in list if the size is over the limit set as 16
        if (previousShuffles.size > Constants.MAX_SHUFFLE__BUFFER_SIZE) {
            previousShuffles.removeAt(0)
        }
        return newIndex
    }
}