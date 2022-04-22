package dev.jaym21.geet.playback.player

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData

interface PlaybackSessionConnector {

    val isConnected: MutableLiveData<Boolean>
    val rootMediaId: String
    val nowPlaying: MutableLiveData<MediaMetadataCompat>
    val queue: MutableLiveData<Queu>
    val playbackState: MutableLiveData<PlaybackStateCompat>
}