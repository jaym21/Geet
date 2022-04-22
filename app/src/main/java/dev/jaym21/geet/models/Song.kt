package dev.jaym21.geet.models

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.annotation.Keep
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.SongUtils
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Song(
    var id: Long = 0,
    var title: String = "",
    var artist: String = "",
    var album: String = "",
    var duration: Long = 0,
    var albumId: Long = 0,
    var artistId: Long = 0,
    var trackNumber: Int = 0
):MediaBrowserCompat.MediaItem(
    MediaDescriptionCompat.Builder()
        .setMediaId(MediaID(Constants.SONG_MODE, "$id").asString())
        .setTitle(title)
        .setIconUri(SongUtils.getAlbumArtUri(albumId))
        .setSubtitle(artist)
        .build(), FLAG_PLAYABLE)

