package dev.jaym21.geet.playback

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import dagger.hilt.android.AndroidEntryPoint
import dev.jaym21.geet.R
import dev.jaym21.geet.extensions.isPlayEnabled
import dev.jaym21.geet.extensions.isPlaying
import dev.jaym21.geet.extensions.toIDList
import dev.jaym21.geet.extensions.toRawMediaItems
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.models.QueueEntity
import dev.jaym21.geet.models.SortType
import dev.jaym21.geet.playback.player.SongPlayer
import dev.jaym21.geet.repository.*
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.NotificationGenerator
import kotlinx.coroutines.*
import javax.inject.Inject

@DelicateCoroutinesApi
@AndroidEntryPoint
class PlaybackService: MediaBrowserServiceCompat(), LifecycleOwner {

    private val lifecycle = LifecycleRegistry(this)
    @Inject lateinit var songRepository: SongsRepository
    @Inject lateinit var queueRepository: QueueRepository
    @Inject lateinit var albumRepository: AlbumRepository
    @Inject lateinit var artistRepository: ArtistRepository
    @Inject lateinit var playlistRepository: PlaylistRepository
    @Inject lateinit var songPlayer: SongPlayer
    @Inject lateinit var notificationGenerator: NotificationGenerator
    private lateinit var noisyCheckReceiver: NoisyCheckReceiver

    override fun getLifecycle() = lifecycle

    override fun onCreate() {
        super.onCreate()
        lifecycle.currentState = Lifecycle.State.RESUMED

        sessionToken = songPlayer.getSession().sessionToken
        noisyCheckReceiver = NoisyCheckReceiver(this, sessionToken!!)

        //checking playback state
        songPlayer.onPlayingState { isPlaying ->
            if (isPlaying) {
                noisyCheckReceiver.register()
                startForeground(Constants.NOTIFICATION_SONG_ID, notificationGenerator.generateNotification(getSession()))
            } else {
                noisyCheckReceiver.unRegister()
                stopForeground(false)
                saveCurrentData()
            }
            notificationGenerator.updateNotification(songPlayer.getSession())
        }

        songPlayer.onCompletion {
            notificationGenerator.updateNotification(songPlayer.getSession())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_STICKY
        }

        val mediaSession = songPlayer.getSession()
        val controller = mediaSession.controller

        when (intent.action) {
            Constants.ACTION_NOTIFICATION_NEXT -> {
                controller.transportControls.skipToNext()
            }
            Constants.ACTION_NOTIFICATION_PLAY_PAUSE -> {
                controller.playbackState?.let { playbackState ->
                    when {
                        playbackState.isPlaying -> controller.transportControls.pause()
                        playbackState.isPlayEnabled -> controller.transportControls.play()
                    }
                }
            }
            Constants.ACTION_NOTIFICATION_PREVIOUS -> {
                controller.transportControls.skipToPrevious()
            }
        }

        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        //selecting caller on the basis if app package name is this app
        val caller = if (clientPackageName == Constants.APP_PACKAGE_NAME){
            MediaID.CALLER_SELF
        } else {
            MediaID.CALLER_OTHER
        }
        return MediaBrowserServiceCompat.BrowserRoot(MediaID(Constants.MEDIA_ID_ROOT.toString(), null, caller).asString(), null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
        GlobalScope.launch(Dispatchers.Main) {
            val mediaItems = withContext(Dispatchers.IO) {
                loadChildren(parentId)
            }
            result.sendResult(mediaItems)
        }
    }

    private fun loadChildren(parentId: String): ArrayList<MediaBrowserCompat.MediaItem> {
        val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
        val mediaIdParent = MediaID().fromString(parentId)
        val mediaType = mediaIdParent.type
        val mediaId = mediaIdParent.mediaId
        val caller = mediaIdParent.caller

        if (mediaType == Constants.MEDIA_ID_ROOT.toString()) {
            addMediaRoots(mediaItems, caller!!)
        } else {
            when (mediaType?.toInt() ?: 0) {
                Constants.ALL_SONGS_MODE -> {
                    mediaItems.addAll(songRepository.getSongs(caller, SortType.A_Z))
                }
                Constants.ALL_ALBUMS_MODE -> {
                    mediaItems.addAll(albumRepository.getAllAlbums(caller))
                }
                Constants.ALL_ARTISTS_MODE -> {
                    mediaItems.addAll(artistRepository.getAllArtists(caller))
                }
                Constants.ALL_PLAYLISTS_MODE -> {
                    mediaItems.addAll(playlistRepository.getAllPlaylists(caller))
                }
                Constants.ALBUM_MODE -> {
                    mediaId?.let {
                        mediaItems.addAll(albumRepository.getSongsForAlbum(caller, it.toLong()))
                    }
                }
                Constants.ARTIST_MODE -> {
                    mediaId?.let {
                        mediaItems.addAll(artistRepository.getSongsForArtist(caller, it.toLong()))
                    }
                }
                Constants.PLAYLIST_MODE -> {
                    mediaId?.let {
                        mediaItems.addAll(playlistRepository.getSongsInPlaylist(caller, it.toLong()))
                    }
                }
            }
        }

        return if (caller == MediaID.CALLER_SELF) {
            mediaItems
        } else {
            mediaItems.toRawMediaItems()
        }
    }

    private fun addMediaRoots(mMediaRoot: MutableList<MediaBrowserCompat.MediaItem>, caller: String) {
        mMediaRoot.add(MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder().apply {
                setTitle(getString(R.string.songs))
                setSubtitle(getString(R.string.songs))
                setIconUri(Constants.EMPTY_ARTWORK_URI.toUri())
                setMediaId(MediaID(Constants.ALL_SONGS_MODE.toString(), null, caller).asString())
            }.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        ))
        mMediaRoot.add(MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder().apply {
                setTitle(getString(R.string.albums))
                setSubtitle(getString(R.string.albums))
                setIconUri(Constants.EMPTY_ARTWORK_URI.toUri())
                setMediaId(MediaID(Constants.ALL_ALBUMS_MODE.toString(), null, caller).asString())
            }.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        ))
        mMediaRoot.add(MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder().apply {
                setTitle(getString(R.string.artists))
                setSubtitle(getString(R.string.artists))
                setIconUri(Constants.EMPTY_ARTWORK_URI.toUri())
                setMediaId(MediaID(Constants.ALL_ARTISTS_MODE.toString(), null, caller).asString())
            }.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        ))
        mMediaRoot.add(MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder().apply {
                setTitle(getString(R.string.playlists))
                setSubtitle(getString(R.string.playlists))
                setIconUri(Constants.EMPTY_ARTWORK_URI.toUri())
                setMediaId(MediaID(Constants.ALL_PLAYLISTS_MODE.toString(), null, caller).asString())
            }.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        ))
    }

    override fun onDestroy() {
        lifecycle.currentState = Lifecycle.State.DESTROYED
        saveCurrentData()
        songPlayer.release()
        super.onDestroy()
    }

    private fun saveCurrentData() {
        GlobalScope.launch(Dispatchers.IO) {
            val mediaSession = songPlayer.getSession()
            val controller = mediaSession.controller

            if (controller == null || controller.playbackState == null || controller.playbackState.state == PlaybackStateCompat.STATE_NONE) {
                return@launch
            }

            //updating current queued songs details
            val queue = controller.queue
            val currentId = controller.metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            queueRepository.updateQueuedSongs(queue?.toIDList(), currentId?.toLong(), lifecycleScope)

            //updating current queue details
            val queueEntity = QueueEntity(
                queueTitle = controller.queueTitle?.toString() ?: getString(R.string.all_songs),
                currentId = currentId?.toLong(),
                currentSeekPosition = controller.playbackState?.position,
                repeatMode = controller.repeatMode,
                shuffleMode = controller.shuffleMode,
                playState = controller.playbackState?.state
            )
            queueRepository.updateQueue(queueEntity)
        }
    }
}