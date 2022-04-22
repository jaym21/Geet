package dev.jaym21.geet.playback.player

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.net.toUri
import dev.jaym21.geet.R
import dev.jaym21.geet.db.QueueDAO
import dev.jaym21.geet.extensions.position
import dev.jaym21.geet.models.QueueEntity
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.repository.SongsRepository
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.SongUtils

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
    fun restoreFromQueue(queue: QueueEntity)
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
): SongPlayer, AudioManager.OnAudioFocusChangeListener {

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

        val sessionIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(context, 0, sessionIntent, 0)
        setSessionActivity(sessionActivityPendingIntent)
        isActive = true
    }

    init {
        queue.setMediaSession(mediaSession)

        //preparing music player with callbacks and seeking to previous position is available from session
        musicPlayer.onPrepared {
            preparedCallback(this@ISongPlayer)
            playSong()
            seekTo(getSession().position().toInt())
        }

        musicPlayer.onCompletion {
            completionCallback(this@ISongPlayer)
            val controller = getSession().controller
            when (controller.repeatMode) {
                PlaybackStateCompat.REPEAT_MODE_ALL -> {
                    controller.transportControls.sendCustomAction(Constants.ACTION_REPEAT_QUEUE, null)
                }
                PlaybackStateCompat.REPEAT_MODE_ONE -> {
                    controller.transportControls.sendCustomAction(Constants.ACTION_REPEAT_SONG, null)
                }
                else -> controller.transportControls.skipToNext()
            }
        }

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_MEDIA)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(this@ISongPlayer, Handler(Looper.getMainLooper()))
                build()
            }
        }
    }

    override fun playSong() {
        //requesting focus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(focusRequest)
        } else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        //confirming current song id in queue
        queue.confirmCurrentId()

        if (isInitialized) {
            //updating playback state as playing if player is not initialized
            updatePlaybackState {
                setState(PlaybackStateCompat.STATE_PLAYING, mediaSession.position(), 1F)
            }
            musicPlayer.play()
            return
        }
        musicPlayer.reset()

        //setting source of song from uri or string path
        val path = SongUtils.getSongUri(queue.currentSongId).toString()
        val isSourceSet = if (path.startsWith("content://")) {
            musicPlayer.setSource(path.toUri())
        } else {
            musicPlayer.setSource(path)
        }
        if (isSourceSet){
            isInitialized = true
            musicPlayer.prepare()
        }
    }

    override fun playSong(id: Long) {
        val song = songsRepository.getSongForId(id)
        playSong(song)
    }

    override fun playSong(song: Song) {
        if (queue.currentSongId != song.id) {
            queue.currentSongId = song.id
            isInitialized = false
            updatePlaybackState {
                setState(PlaybackStateCompat.STATE_STOPPED, 0, 1F)
            }
        }
        setMetaData(song)
        playSong()
    }

    override fun pause() {
        if (musicPlayer.isPlaying() && isInitialized) {
            musicPlayer.pause()
            updatePlaybackState {
                setState(PlaybackStateCompat.STATE_PAUSED, mediaSession.position(), 1F)
            }
        }
    }

    override fun stop() {

    }

    override fun release() {

    }

    override fun nextSong() {

    }

    override fun previousSong() {

    }

    override fun repeatSong() {

    }

    override fun playNext(id: Long) {

    }

    override fun seekTo(position: Int) {

    }

    override fun setQueue(ids: LongArray, title: String) {

    }

    override fun repeatQueue() {

    }

    override fun removeFromQueue(id: Long) {

    }

    override fun restoreFromQueue(queue: QueueEntity) {

    }

    override fun swapQueueSongs(from: Int, to: Int) {

    }

    override fun onPlayingState(playing: OnIsPlaying) {

    }

    override fun getSession(): MediaSessionCompat = mediaSession

    override fun onPrepared(prepared: OnPrepared<SongPlayer>) {

    }

    override fun onCompletion(completion: OnCompletion<SongPlayer>) {

    }

    override fun onError(error: OnError<SongPlayer>) {

    }

    override fun setPlaybackState(state: PlaybackStateCompat) {

    }

    override fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit) {

    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> playSong()
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
        }
    }

    //setting current song meta data in media session
    private fun setMetaData(song: Song) {
        //getting song artwork from album
        val artwork = SongUtils.getAlbumArtBitmap(context, song.albumId)
        //making media meta data
        val mediaMetadata = metadataBuilder.apply {
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
            putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.albumId.toString())
            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id.toString())
        }.build()
        mediaSession.setMetadata(mediaMetadata)
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