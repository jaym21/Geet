package dev.jaym21.geet.playback.player

import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
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
}