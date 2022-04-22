package dev.jaym21.geet.playback.player

import androidx.lifecycle.MutableLiveData

interface PlaybackSessionConnector {

    val isConnected: MutableLiveData<Boolean>
}