package dev.jaym21.geet.extensions

import android.support.v4.media.session.MediaSessionCompat
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.repository.SongsRepository
import dev.jaym21.geet.utils.SongUtils
import java.util.*

fun <T> List<T>?.moveElement(fromIndex: Int, toIndex: Int): List<T> {
    if (this == null) {
        return Collections.emptyList()
    }
    return toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
}

fun LongArray.toQueue(songsRepository: SongsRepository): List<MediaSessionCompat.QueueItem> {
    val songList = songsRepository.getSongsForIds(this)
    //keeping the original order of songs as the ids array
    songList.keepInOrder(this)?.let {
        return it.toQueue()
    }?: return songList.toQueue()
}

fun List<Song>.keepInOrder(queue: LongArray): List<Song>? {
    //this may happen if user deletes some item from his library and then comes back to app after we stored the current queue ids
    //if the two arrays are different return the array as is
    if (size != queue.size) return this
    return if (isNotEmpty() && queue.isNotEmpty()) {
        val keepOrderList = Array(size, init = { Song() })
        forEach {
            keepOrderList[queue.indexOf(it.id)] = it
        }
        keepOrderList.asList()
    } else null
}

fun <T> List<T>.equalsBy(other: List<T>, by: (left: T, right: T) -> Boolean): Boolean {
    if (this.size != other.size) {
        return false
    }
    for ((index, item) in withIndex()) {
        val otherItem = other[index]
        val itemsEqual = by(item, otherItem)
        if (!itemsEqual) {
            return false
        }
    }
    return true
}

fun LongArray.convertToString(): String {
    var result = ""
    for (i in this) {
        result = "$result $i"
    }
    return result
}

fun List<Song>.reorderByIds(ids: LongArray): List<Song> {
    val originalSongs = this
    val result = mutableListOf<Song>()
    for (id in ids) {
        val song = SongUtils.getSongWithId(originalSongs, id)
        if (song != null) {
            result.add(song)
        }
    }
    return result
}