package dev.jaym21.geet.models

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import dev.jaym21.geet.utils.Constants
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Playlist(
    val id: Long,
    val name: String,
    val noOfSong: Int,
    var albumIds: List<Long> = emptyList()
) : MediaBrowserCompat.MediaItem(
    MediaDescriptionCompat.Builder()
        .setMediaId(MediaID(Constants.PLAYLIST_MODE.toString(), id.toString()).asString())
        .setTitle(name)
        .setSubtitle("$noOfSong songs")
        .build(), FLAG_BROWSABLE
)