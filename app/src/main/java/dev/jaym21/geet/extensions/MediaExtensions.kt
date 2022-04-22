package dev.jaym21.geet.extensions

import android.support.v4.media.session.MediaSessionCompat

fun MediaSessionCompat.position(): Long {
    return controller.playbackState.position
}