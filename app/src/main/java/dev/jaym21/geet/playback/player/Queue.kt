package dev.jaym21.geet.playback.player

import android.app.Application
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import dev.jaym21.geet.R
import dev.jaym21.geet.db.QueueDAO
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.repository.SongsRepository
import dev.jaym21.geet.utils.Constants
import java.util.*

//manage functions related to queue
interface Queue {

    var ids: LongArray
    var title: String
    var currentSongId: Long
    val currentSongIndex: Int
    val nextSongIndex: Int?
    val nextSongId: Long?
    val previousSongId: Long?
    fun swap(from: Int, to: Int)
    fun moveToNext(id: Long)
    fun remove(id: Long)
    fun reset()
    fun firstId(): Long
    fun lastId(): Long
    fun currentSong(): Song
    fun confirmCurrentId()
    fun queueItems(): List<MediaSessionCompat.QueueItem>
    fun setMediaSession(session: MediaSessionCompat)
}

class IQueue(
    private val context: Application,
    private val songsRepository: SongsRepository,
    private val queueDao: QueueDAO
): Queue {

    private lateinit var session: MediaSessionCompat
    private val shuffleRandom = Random()
    private val previousShuffles = mutableListOf<Int>()

    override var ids = LongArray(0)
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                session.setQueue(queueItems())
            }
        }
    override var title: String = context.getString(R.string.all_songs)
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
    override var currentSongId: Long = Constants.NO_SONG_ID
    override val currentSongIndex: Int
        get() = ids.indexOf(currentSongId)
    override val nextSongIndex: Int?
        get() {
            val nextIndex = currentSongIndex + 1
            val controller = session.controller
            return when {
                controller.shuffleMode == SHUFFLE_MODE_ALL -> getShuffleIndex()
            }
        }
    override val nextSongId: Long?,
    override val previousSongId: Long?


    override fun swap(from: Int, to: Int) {

    }

    override fun moveToNext(id: Long) {
        TODO("Not yet implemented")
    }

    override fun remove(id: Long) {
        TODO("Not yet implemented")
    }

    override fun reset() {
        TODO("Not yet implemented")
    }

    override fun firstId(): Long {
        TODO("Not yet implemented")
    }

    override fun lastId(): Long {
        TODO("Not yet implemented")
    }

    override fun currentSong(): Song {
        TODO("Not yet implemented")
    }

    override fun confirmCurrentId() {
        TODO("Not yet implemented")
    }

    override fun queueItems(): List<MediaSessionCompat.QueueItem> {
        TODO("Not yet implemented")
    }

    override fun setMediaSession(session: MediaSessionCompat) {
        TODO("Not yet implemented")
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