package dev.jaym21.geet.viewmodels

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jaym21.geet.extensions.isPlayEnabled
import dev.jaym21.geet.extensions.isPlaying
import dev.jaym21.geet.extensions.isPrepared
import dev.jaym21.geet.extensions.map
import dev.jaym21.geet.models.Event
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.playback.player.PlaybackSessionConnector
import dev.jaym21.geet.repository.SongsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val songsRepository: SongsRepository,
    private val playbackSessionConnector: PlaybackSessionConnector
): ViewModel() {


    val rootMediaId: LiveData<MediaID> = playbackSessionConnector.isConnected.map { isConnected ->
        if (isConnected) {
            MediaID().fromString(playbackSessionConnector.rootMediaId)
        } else {
            null
        }
    }

    val mediaController: LiveData<MediaControllerCompat> = playbackSessionConnector.isConnected.map { isConnected ->
        if (isConnected) {
            playbackSessionConnector.mediaController
        } else {
            null
        }
    }

    private val _songs: MutableLiveData<List<Song>> =  MutableLiveData()
    val songs: LiveData<List<Song>> = _songs

    private val _songsForIds: MutableLiveData<List<Song>> =  MutableLiveData()
    val songsForIds: LiveData<List<Song>> = _songsForIds

    private val _songForId: MutableLiveData<Song> = MutableLiveData()
    val songForId: LiveData<Song> = _songForId

    private val _navigateToMediaItem = MutableLiveData<Event<MediaID>>()
    val navigateToMediaItem: LiveData<Event<MediaID>> = _navigateToMediaItem

    fun transportControls() = playbackSessionConnector.transportControls

    fun mediaItemClicked(clickedItem: MediaBrowserCompat.MediaItem, extras: Bundle?) {
        if (clickedItem.isBrowsable) {
            browseToItem(clickedItem)
        } else {
            playMedia(clickedItem, extras)
        }
    }

    private fun browseToItem(clickedItem: MediaBrowserCompat.MediaItem) {
        _navigateToMediaItem.value = Event(MediaID().fromString(clickedItem.mediaId!!).apply {
            this.mediaItem = clickedItem
        })
    }

    private fun playMedia(mediaItem: MediaBrowserCompat.MediaItem, extras: Bundle?) {
        val nowPlaying = playbackSessionConnector.nowPlaying.value
        val transportControls = playbackSessionConnector.transportControls
        val isPrepared = playbackSessionConnector.playbackState.value?.isPrepared ?: false

        if (isPrepared && MediaID().fromString(mediaItem.mediaId!!).mediaId == nowPlaying?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) {
            playbackSessionConnector.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.d("TAGYOYO", "Media item clicked can be neither played nor paused is enabled: mediaId=${mediaItem.mediaId}")
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(mediaItem.mediaId, extras)
        }
    }

    fun loadSongs() = viewModelScope.launch(Dispatchers.IO) {
//        _songs.postValue(songsRepository.getSongs())
    }

    fun getSongForIds(ids: LongArray) = viewModelScope.launch {
        _songsForIds.postValue(songsRepository.getSongsForIds(ids))
    }

    fun getSongForId(id: Long) = viewModelScope.launch {
        _songForId.postValue(songsRepository.getSongForId(id))
    }
}