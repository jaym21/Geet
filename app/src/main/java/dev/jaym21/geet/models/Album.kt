package dev.jaym21.geet.models

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.SongUtils
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Album(
    var id: Long = 0,
    var albumTitle: String = "",
    var artist: String = "",
    var artistId: Long = 0,
    var noOfSongs: Int = 0,
    var year: Int = 0
) : MediaBrowserCompat.MediaItem(
    MediaDescriptionCompat.Builder()
        .setMediaId(MediaID(Constants.ALBUM_MODE.toString(), id.toString()).asString())
        .setTitle(albumTitle)
        .setIconUri(SongUtils.getAlbumArtUri(id))
        .setSubtitle(artist)
        .build(), FLAG_BROWSABLE
)