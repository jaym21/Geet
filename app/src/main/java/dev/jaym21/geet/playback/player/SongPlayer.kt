package dev.jaym21.geet.playback.player

import android.app.Application
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import dev.jaym21.geet.R
import dev.jaym21.geet.db.QueueDAO
import dev.jaym21.geet.models.QueueEntity
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.repository.SongsRepository

typealias OnIsPlaying = ISongPlayer.(playing: Boolean) -> Unit

//Wrapper interface around Music Player for playing songs through queues
interface SongPlayer {

    fun playSong()
    fun playSong(id: Long)
    fun playSong(song: Song)
    fun pause()
    fun stop()
    fun release()
    fun nextSong()
    fun previousSong()
    fun repeatSong()
    fun playNext(id: Long)
    fun seekTo(position: Int)
    fun setQueue(ids: LongArray = LongArray(0), title: String = "")
    fun repeatQueue()
    fun removeFromQueue(id: Long)
    fun restoreFromQueueData(queue: QueueEntity)
    fun swapQueueSongs(from: Int, to: Int)
    fun onPlayingState(playing: OnIsPlaying)
    fun getSession(): MediaSessionCompat
    fun onPrepared(prepared: OnPrepared<SongPlayer>)
    fun onCompletion(completion: OnCompletion<SongPlayer>)
    fun onError(error: OnError<SongPlayer>)
    fun setPlaybackState(state: PlaybackStateCompat)
    fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit)
}

class ISongPlayer(
    private val context: Application,
    private val musicPlayer: MusicPlayer,
    private val songsRepository: SongsRepository,
    private val queueDao: QueueDAO,
    private val queue: Queue
): SongPlayer {

    private var isInitialized: Boolean = false

    private var isPlayingCallback: OnIsPlaying = {}
    private var preparedCallback: OnPrepared<SongPlayer> = {}
    private var completionCallback: OnCompletion<SongPlayer> = {}
    private var errorCallback: OnError<SongPlayer> = {}

    private var metadataBuilder = MediaMetadataCompat.Builder()
    private var stateBuilder = createDefaultPlaybackState()

    private lateinit var audioManager: AudioManager
    private lateinit var focusRequest: AudioFocusRequest

    private var mediaSession = MediaSessionCompat(context, context.getString(R.string.app_name)).apply {
        setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        setCallback(MediaSessionCallback(this, this@ISongPlayer, songsRepository, queueDao))
        setPlaybackState(stateBuilder.build())
    }
    override fun playSong() {

    }

    override fun playSong(id: Long) {
        TODO("Not yet implemented")
    }

    override fun playSong(song: Song) {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override fun nextSong() {
        TODO("Not yet implemented")
    }

    override fun previousSong() {
        TODO("Not yet implemented")
    }

    override fun repeatSong() {
        TODO("Not yet implemented")
    }

    override fun playNext(id: Long) {
        TODO("Not yet implemented")
    }

    override fun seekTo(position: Int) {
        TODO("Not yet implemented")
    }

    override fun setQueue(ids: LongArray, title: String) {
        TODO("Not yet implemented")
    }

    override fun repeatQueue() {
        TODO("Not yet implemented")
    }

    override fun removeFromQueue(id: Long) {
        TODO("Not yet implemented")
    }

    override fun restoreFromQueueData(queue: QueueEntity) {
        TODO("Not yet implemented")
    }

    override fun swapQueueSongs(from: Int, to: Int) {
        TODO("Not yet implemented")
    }

    override fun onPlayingState(playing: OnIsPlaying) {
        TODO("Not yet implemented")
    }

    override fun getSession(): MediaSessionCompat {
        TODO("Not yet implemented")
    }

    override fun onPrepared(prepared: OnPrepared<SongPlayer>) {
        TODO("Not yet implemented")
    }

    override fun onCompletion(completion: OnCompletion<SongPlayer>) {
        TODO("Not yet implemented")
    }

    override fun onError(error: OnError<SongPlayer>) {
        TODO("Not yet implemented")
    }

    override fun setPlaybackState(state: PlaybackStateCompat) {
        TODO("Not yet implemented")
    }

    override fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit) {
        TODO("Not yet implemented")
    }
}

private fun createDefaultPlaybackState(): PlaybackStateCompat.Builder {
    return PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
                or PlaybackStateCompat.ACTION_SET_REPEAT_MODE
    )
        .setState(PlaybackStateCompat.STATE_NONE, 0, 1f)
}