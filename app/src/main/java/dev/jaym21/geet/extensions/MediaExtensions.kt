package dev.jaym21.geet.extensions

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import dev.jaym21.geet.utils.Constants

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

fun getExtraBundle(queue: LongArray, title: String): Bundle? {
    return getExtraBundle(queue, title, 0)
}

fun getExtraBundle(queue: LongArray, title: String, seekTo: Int?): Bundle? {
    val bundle = Bundle()
    bundle.putLongArray(Constants.QUEUED_SONGS_LIST, queue)
    bundle.putString(Constants.QUEUE_TITLE, title)
    if (seekTo != null)
        bundle.putInt(Constants.SEEK_TO_POSITION, seekTo)
    else bundle.putInt(Constants.SEEK_TO_POSITION, 0)
    return bundle
}