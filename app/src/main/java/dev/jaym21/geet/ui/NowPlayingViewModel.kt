package dev.jaym21.geet.ui

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jaym21.geet.models.MetaData
import dev.jaym21.geet.models.QueueData
import dev.jaym21.geet.playback.player.PlaybackSessionConnector
import dev.jaym21.geet.utils.Constants
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playbackSessionConnector: PlaybackSessionConnector
): ViewModel() {

    private val _currentData = MutableLiveData<MetaData>()
    val currentData: LiveData<MetaData> = _currentData

    private val _queueData = MutableLiveData<QueueData>()
    val queueData: LiveData<QueueData> = _queueData

    //updates in media meta data are updated in currentData live data
    private val mediaMetadataObserve = Observer<MediaMetadataCompat> { mediaMetadata ->
        mediaMetadata?.let {
            val newValue = _currentData.value?.getMediaMetaData(it) ?: MetaData().getMediaMetaData(it)
            _currentData.postValue(newValue)
        }
    }

    //updates in media playback state are updated in currentData live data
    private val playbackStateObserver = Observer<PlaybackStateCompat> { playbackState ->
        playbackState?.let {
            val newValue = _currentData.value?.getPlaybackState(it) ?: MetaData().getPlaybackState(it)
            _currentData.postValue(newValue)
        }
    }

    //updating the latest queue data
    private val queueDataObserver = Observer<QueueData> { queueData ->
        queueData?.let {
            _queueData.postValue(it)
        }
    }

    //updating playbackSessionConnector
    private val playbackSessionConnection = playbackSessionConnector.apply {
        nowPlaying.observeForever(mediaMetadataObserve)
        playbackState.observeForever(playbackStateObserver)
        queueData.observeForever(queueDataObserver)
        isConnected.observeForever { isConnected ->
            if (isConnected) {
                transportControls.sendCustomAction(Constants.ACTION_SET_MEDIA_STATE, null)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackSessionConnection.nowPlaying.removeObserver(mediaMetadataObserve)
        playbackSessionConnection.playbackState.removeObserver(playbackStateObserver)
    }
}