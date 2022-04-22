package dev.jaym21.geet.models

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import dev.jaym21.geet.extensions.toIDList

data class QueueData(
    var queueTitle: String = "All Songs",
    var currentId: Long = 0,
    var queue: LongArray = LongArray(0)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QueueData

        if (queueTitle != other.queueTitle) return false
        if (!queue.contentEquals(other.queue)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = queueTitle.hashCode()
        result = 31 * result + queue.contentHashCode()
        return result
    }

     fun findFromMediaController(mediaControllerCompat: MediaControllerCompat?): QueueData {
        mediaControllerCompat?.let {
            return QueueData(
                mediaControllerCompat.queueTitle?.toString().orEmpty().let {
                    if (it.isEmpty())
                        "All Songs"
                    else
                        it
                },
                mediaControllerCompat.metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.toLong() ?: 0,
                mediaControllerCompat.queue?.toIDList() ?: LongArray(0)
            )
        }
         return QueueData()
     }
}