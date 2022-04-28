package dev.jaym21.geet.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.playback.player.PlaybackSessionConnector

class PlaybackSessionProviderFactory(private val mediaID: MediaID, private val playbackSessionConnector: PlaybackSessionConnector): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlaybackSessionViewModel(mediaID, playbackSessionConnector) as T
    }
}