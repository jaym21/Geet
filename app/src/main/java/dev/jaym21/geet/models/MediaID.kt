package dev.jaym21.geet.models

import android.support.v4.media.MediaBrowserCompat

class MediaID(var type: String? = null, var mediaId: String? = "NA", var caller: String? = currentCaller) {

    var mediaItem: MediaBrowserCompat.MediaItem? = null

    companion object {
        const val CALLER_SELF = "self"
        const val CALLER_OTHER = "other"
        private const val TYPE = "type: "
        private const val MEDIA_ID = "media_id: "
        private const val CALLER = "caller: "
        private const val SEPARATOR = " | "
        var currentCaller: String? = MediaID.CALLER_SELF
    }

    fun asString(): String {
        return TYPE + type + SEPARATOR + MEDIA_ID + mediaId + SEPARATOR + CALLER + caller
    }

    fun fromString(s: String): MediaID {
        this.type = s.substring(6, s.indexOf(SEPARATOR))
        this.mediaId = s.substring(s.indexOf(SEPARATOR)+ 3 + 10, s.lastIndexOf(SEPARATOR))
        this.caller = s.substring(s.lastIndexOf(SEPARATOR) + 3 + 8, s.length)
        return this
    }
}