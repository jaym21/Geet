package dev.jaym21.geet.playback.player

import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import dev.jaym21.geet.db.QueueDAO
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.repository.SongsRepository
import dev.jaym21.geet.utils.Constants

class MediaSessionCallback(
    private val mediaSession: MediaSessionCompat,
    private val songPlayer: SongPlayer,
    private val songsRepository: SongsRepository,
    private val queueDAO: QueueDAO
): MediaSessionCompat.Callback() {

    override fun onPlay() = songPlayer.playSong()

    override fun onPause() = songPlayer.pause()

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        val songId = MediaID().fromString(mediaId).mediaId!!.toLong()
        songPlayer.playSong(songId)

        if (extras == null)
            return

        val queue = extras.getLongArray(Constants.QUEUED_SONGS_LIST)
        val queueTitle = extras.getString(Constants.QUEUE_TITLE) ?: ""
        val seekToPosition = extras.getInt(Constants.SEEK_TO_POSITION)

        if (queue != null) {
            songPlayer.setQueue(queue, queueTitle)
        }
        if (seekToPosition > 0) {
            songPlayer.seekTo(seekToPosition)
        }
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
        query?.let {
            val song = songsRepository.searchSongs(query, 1)
            if (song.isNotEmpty()) {
                songPlayer.playSong(song.first())
            }
        }?: onPlay()
    }

    override fun onSeekTo(pos: Long) = songPlayer.seekTo(pos.toInt())

    override fun onSkipToNext() = songPlayer.nextSong()

    override fun onSkipToPrevious() = songPlayer.previousSong()

    override fun onStop() = songPlayer.stop()

    override fun onSetRepeatMode(repeatMode: Int) {
        super.onSetRepeatMode(repeatMode)
        val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
        songPlayer.setPlaybackState(
            PlaybackStateCompat.Builder(mediaSession.controller.playbackState)
                .setExtras(bundle.apply {
                    putInt(Constants.REPEAT_MODE, repeatMode)
                }).build()
        )
    }

    override fun onSetShuffleMode(shuffleMode: Int) {
        super.onSetShuffleMode(shuffleMode)
        val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
        songPlayer.setPlaybackState(
            PlaybackStateCompat.Builder(mediaSession.controller.playbackState)
                .setExtras(bundle.apply {
                    putInt(Constants.SHUFFLE_MODE, shuffleMode)
                }).build()
        )
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        when (action) {
            Constants.ACTION_PLAY_NEXT -> {
                val nextSongId = extras!!.getLong(Constants.SONG)
                songPlayer.playNext(nextSongId)
            }
            Constants.ACTION_SONG_DELETED -> {
                val id = extras!!.getLong(Constants.SONG)
                songPlayer.removeFromQueue(id)
            }
            Constants.ACTION_QUEUE_REORDER -> {
                val from = extras!!.getInt(Constants.QUEUE_FROM)
                val to = extras.getInt(Constants.QUEUE_TO)
                songPlayer.swapQueueSongs(from, to)
            }
            Constants.ACTION_REPEAT_SONG -> songPlayer.repeatSong()
            Constants.ACTION_REPEAT_QUEUE -> songPlayer.repeatQueue()
            Constants.ACTION_SET_MEDIA_STATE -> setSavedMediaSessionState()
            Constants.ACTION_RESTORE_MEDIA_SESSION -> restoreMediaSession()
        }
    }

    private fun setSavedMediaSessionState() {
        // Only set saved session from db if we know there is not any active media session
        val controller = mediaSession.controller ?: return
        if (controller.playbackState == null || controller.playbackState.state == PlaybackStateCompat.STATE_NONE) {
            val queue = queueDAO.getQueue() ?: return
            songPlayer.restoreFromQueue(queue)
        } else {
            // Force update the playback state and metadata from the media session so that the
            // attached Observer in NowPlayingViewModel gets the current state.
            restoreMediaSession()
        }
    }

    private fun restoreMediaSession() {
        songPlayer.setPlaybackState(mediaSession.controller.playbackState)
        mediaSession.setMetadata(mediaSession.controller.metadata)
    }
}