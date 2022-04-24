package dev.jaym21.geet.extensions

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat

fun MediaSessionCompat.position(): Long {
    return controller.playbackState.position
}

fun ArrayList<MediaBrowserCompat.MediaItem>.toRawMediaItems(): ArrayList<MediaBrowserCompat.MediaItem> {
    val list = arrayListOf<MediaBrowserCompat.MediaItem>()
    forEach {
        list.add(
            MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(it.description.mediaId)
                .setTitle(it.description.title)
                .setIconUri(it.description.iconUri)
                .setSubtitle(it.description.subtitle)
                .build(), it.flags))
    }
    return list
}