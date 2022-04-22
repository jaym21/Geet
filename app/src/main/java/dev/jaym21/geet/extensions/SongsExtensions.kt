package dev.jaym21.geet.extensions

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.SongUtils

fun List<Song?>.toQueue(): List<MediaSessionCompat.QueueItem> {
    return filterNotNull().map {
        MediaSessionCompat.QueueItem(it.toDescription(), it.id)
    }
}

fun Song.toDescription(): MediaDescriptionCompat {
    return MediaDescriptionCompat.Builder()
        .setTitle(title)
        .setMediaId(id.toString())
        .setSubtitle(artist)
        .setDescription(album)
        .setIconUri(SongUtils.getAlbumArtUri(albumId)).build()
}