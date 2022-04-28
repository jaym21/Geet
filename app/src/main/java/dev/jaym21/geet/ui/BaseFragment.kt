package dev.jaym21.geet.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import dev.jaym21.geet.R
import dev.jaym21.geet.extensions.filter
import dev.jaym21.geet.extensions.map
import dev.jaym21.geet.extensions.safeActivity
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.playback.player.PlaybackSessionConnector
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.viewmodels.*
import javax.inject.Inject

open class BaseFragment: Fragment() {

    protected val mainViewModel by activityViewModels<MainViewModel>()
    protected val navigationViewModel by activityViewModels<NavigationViewModel>()
    protected val nowPlayingViewModel by activityViewModels<NowPlayingViewModel>()
    @Inject lateinit var playbackSessionConnector: PlaybackSessionConnector
    protected var playbackSessionViewModel: PlaybackSessionViewModel? = null
    private var mediaID: MediaID? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.navigateToMediaItem
            .map { it.getContentIfNotHandled() }
            .filter { it != null }
            .observe(this) {
                mediaID = it
                if (mediaID != null) {
                    initializePlaybackSession(mediaID!!)
                    navigateToMediaItem(mediaID!!)
                }
            }
    }

    private fun initializePlaybackSession(mediaId: MediaID) {
        playbackSessionViewModel = ViewModelProvider(this, PlaybackSessionProviderFactory(mediaId, playbackSessionConnector)).get(PlaybackSessionViewModel::class.java)
    }

    private fun navigateToMediaItem(mediaId: MediaID) {
        when (mediaId.type?.toInt()) {
            Constants.ALL_SONGS_MODE -> {

            }
            Constants.ALL_ALBUMS_MODE -> {

            }
            Constants.ALL_ARTISTS_MODE -> {

            }
            Constants.ALL_PLAYLISTS_MODE -> {

            }
            Constants.ALL_GENRES_MODE -> {

            }
            Constants.ARTIST_MODE -> {

            }
            Constants.ALBUM_MODE -> {

            }
            Constants.PLAYLIST_MODE -> {

            }
            Constants.GENRE_MODE -> {

            }
            else -> {}
        }
    }
}