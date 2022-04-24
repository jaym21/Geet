package dev.jaym21.geet.models

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import dev.jaym21.geet.utils.Constants
import kotlinx.parcelize.Parcelize

@Parcelize
data class Genre(
    val id: Long,
    val name: String,
    val noOfSong: Int
): MediaBrowserCompat.MediaItem(
    MediaDescriptionCompat.Builder()
        .setMediaId(MediaID("${Constants.GENRE_MODE}", "$id").asString())
        .setTitle(name)
        .setSubtitle("$noOfSong songs")
        .build(), FLAG_BROWSABLE
)