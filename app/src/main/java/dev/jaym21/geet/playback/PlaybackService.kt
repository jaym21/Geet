package dev.jaym21.geet.playback

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.media.MediaBrowserServiceCompat

class PlaybackService: MediaBrowserServiceCompat(), LifecycleOwner {

    private val lifecycle = LifecycleRegistry(this)

    override fun getLifecycle() = lifecycle

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {

    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {

    }


}