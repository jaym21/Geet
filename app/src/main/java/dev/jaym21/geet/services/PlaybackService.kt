package dev.jaym21.geet.services

import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.repository.SongsRepository
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.NotificationGenerator
import dev.jaym21.geet.utils.PreferencesHelper
import dev.jaym21.geet.utils.SongUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class PlaybackService: Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnSeekCompleteListener, AudioManager.OnAudioFocusChangeListener {

    private val repository = SongsRepository(applicationContext)
    private val playbackBind: IBinder = PlaybackBinder()
    private lateinit var audioManager: AudioManager
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var notificationGenerator: NotificationGenerator
    private lateinit var handler: Handler
    private var viewSongInterface: ViewSongInterface? = null
    private var playbackServiceInterface: PlaybackServiceInterface? = null
    private var queuedSongs = listOf<Song>()
    var songState = ""
    private var songPosition = 0

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Main)

    override fun onBind(p0: Intent?): IBinder {
        return playbackBind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mediaPlayer.stop()
        mediaPlayer.release()
        return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        init()
        initPlayer()
    }

    private fun init() {
        audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val queueIds = PreferencesHelper.getQueueIds(applicationContext)
        serviceScope.launch {
            queuedSongs = repository.getSongsForIds(queueIds)
        }

        handler = Handler(Looper.getMainLooper())
    }

    private fun initPlayer() {
        mediaPlayer = MediaPlayer()

        mediaPlayer.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer.setAudioAttributes(audioAttributes)
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnErrorListener(this)
        mediaPlayer.setOnBufferingUpdateListener(this)
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnSeekCompleteListener(this)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (mediaPlayer.currentPosition > 0) {
            if (songPosition != queuedSongs.size - 1)
                mediaPlayer.reset()

            playNextSong()
        }
    }

    override fun onError(mp: MediaPlayer?, p1: Int, p2: Int): Boolean {
        mediaPlayer.reset()
        return false
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, p1: Int) {

    }

    override fun onPrepared(mp: MediaPlayer?) {
        //TODO: use non deprecated code
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        mediaPlayer.start()
        setState(Constants.SONG_STARTED)
        notificationGenerator.updateView(true, songPosition)

        handler.postDelayed(Runnable {
            val current = mediaPlayer.currentPosition
            if (viewSongInterface != null)
                viewSongInterface?.onSongProgress(current / 100)

            if (songPosition < queuedSongs.size)
                playbackServiceInterface?.onSongProgress(((current * 10000) / queuedSongs[songPosition].duration).toInt())

            handler.postDelayed({}, 100)
        }, 100)
    }

    override fun onSeekComplete(mp: MediaPlayer?) {

    }

    override fun onAudioFocusChange(i: Int) {
        when (i) {
            AudioManager.AUDIOFOCUS_GAIN, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (songState == Constants.SONG_PLAYED)
                    playSong()
            }
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pauseSong()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    fun startSong() {
        val song = queuedSongs[songPosition]

        showNotification(song)
        setState(Constants.SONG_LOADED)

        playbackServiceInterface?.onSongChanged(songPosition)
        if (viewSongInterface != null)
            viewSongInterface?.onSongChanged(songPosition)

        mediaPlayer.reset()

        val path = SongUtils.getSongUri(song.id).toString()
        var isSourceSet = if (path.startsWith("content://")) {
            setSource(path.toUri())
        } else {
            setSource(path)
        }

        if (isSourceSet) {
            mediaPlayer.prepare()
        }
    }

    fun playSong() {
        if (songState.isNotEmpty()) {
            //TODO: use non deprecated code
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            mediaPlayer.start()
            setState(Constants.SONG_PLAYED)
            notificationGenerator.updateView(true,  songPosition)
        } else {
            startSong()
        }
    }

    fun pauseSong() {
        mediaPlayer.pause()
        setState(Constants.SONG_PAUSED)
        notificationGenerator.updateView(false, songPosition)
    }

    fun playNextSong() {
        //TODO: CHECK SHUFFLE
        if (songPosition == queuedSongs.size -1) {
            if (PreferencesHelper.getIsRepeatOn(applicationContext)) {
                songPosition = 0
            } else {
                setState(Constants.SONG_ENDED)
                notificationGenerator.updateView(false, songPosition)
            }
        } else {
            songPosition++
        }
        startSong()
    }

    fun playPreviousSong() {
        //TODO: CHECK SHUFFLE
        if (songPosition == 0) {
            if (PreferencesHelper.getIsRepeatOn(applicationContext)) {
                songPosition = queuedSongs.size - 1
            } else {
                setState(Constants.SONG_ENDED)
                notificationGenerator.updateView(false, songPosition)
            }
        } else {
            songPosition--
        }
        startSong()
    }

    fun seek(position: Int) {
        mediaPlayer.seekTo(position)
    }

    private fun setState(state: String) {
        songState = state

        playbackServiceInterface?.onSongDisturbed(state, queuedSongs[songPosition])
        if (viewSongInterface != null)
            viewSongInterface?.onSongDisturbed(state, queuedSongs[songPosition])
    }

    private fun showNotification(song: Song) {
        notificationGenerator = NotificationGenerator()
        val albumArtUri = SongUtils.getAlbumArtUri(song.albumId)
        Glide.with(applicationContext)
            .asBitmap()
            .load(albumArtUri)
            .into(object: CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    notificationGenerator.showSongNotification(applicationContext, songPosition, queuedSongs, resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    notificationGenerator.showSongNotification(applicationContext, songPosition, queuedSongs, null)
                }

            })
    }


    private fun setSource(path: String): Boolean {
        try {
            mediaPlayer.setDataSource(path)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun setSource(uri: Uri): Boolean {
        try {
            mediaPlayer.setDataSource(applicationContext, uri)
        }  catch (e: Exception) {
            return false
        }
        return true
    }

    inner class PlaybackBinder: Binder() {
        fun getService(): PlaybackService {
            return this@PlaybackService
        }
    }

    fun setPlaybackServiceCallbacks(playbackServiceInterface: PlaybackServiceInterface) {
        this.playbackServiceInterface = playbackServiceInterface
    }

    fun setViewSongInterface(viewSongInterface: ViewSongInterface) {
        this.viewSongInterface = viewSongInterface
    }

    interface PlaybackServiceInterface {
        fun onSongDisturbed(state: String, song: Song)

        fun onSongChanged(newPosition: Int)

        fun onSongProgress(position: Int)
    }

    interface ViewSongInterface {
        fun onSongDisturbed(state: String, song: Song)

        fun onSongChanged(newPosition: Int)

        fun onSongProgress(position: Int)
    }
}