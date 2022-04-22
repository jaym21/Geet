package dev.jaym21.geet.playback.player

import android.support.v4.media.session.MediaSessionCompat
import dev.jaym21.geet.db.QueueDAO
import dev.jaym21.geet.repository.SongsRepository

class MediaSessionCallback(
    private val mediaSession: MediaSessionCompat,
    private val songPlayer: SongPlayer,
    private val songsRepository: SongsRepository,
    private val queueDAO: QueueDAO
): MediaSessionCompat.Callback() {

    override fun onPlay() = songPlayer.playSong()

    override fun onPause() = songPlayer.pause()
}