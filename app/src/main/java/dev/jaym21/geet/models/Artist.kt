package dev.jaym21.geet.models

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import dev.jaym21.geet.utils.Constants
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist (
    var id: Long = 0,
    var name: String = "",
    var noOfSongs: Int = 0,
    var noOfAlbums: Int = 0,
    var albumIds: List<Long> = emptyList()
) : MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setMediaId(MediaID(Constants.ARTIST_MODE.toString(), id.toString()).asString())
            .setTitle(name)
            .setSubtitle("$noOfAlbums albums")
            .build(), FLAG_BROWSABLE)