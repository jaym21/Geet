package dev.jaym21.geet.viewmodels

import android.media.browse.MediaBrowser
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.playback.player.PlaybackSessionConnector
import javax.inject.Inject

@HiltViewModel
class PlaybackSessionViewModel @Inject constructor(private val mediaID: MediaID, playbackSessionConnector: PlaybackSessionConnector): ViewModel() {

    private val _mediaItems = MutableLiveData<List<MediaBrowserCompat.MediaItem>>()
    val mediaItems: LiveData<List<MediaBrowserCompat.MediaItem>> = _mediaItems

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
            _mediaItems.postValue(children)
        }
    }
}