package dev.jaym21.geet.playback.player

import android.support.v4.media.session.MediaSessionCompat
import dev.jaym21.geet.models.Song

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