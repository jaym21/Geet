package dev.jaym21.geet.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.repository.SongsRepository
import dev.jaym21.geet.utils.PreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PlaybackService: Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnSeekCompleteListener {

    private val repository = SongsRepository(applicationContext)
    private val playbackBind: IBinder = PlaybackBinder()
    private lateinit var audioManager: AudioManager
    private lateinit var mediaPlayer: MediaPlayer
    private var queuedSongs = listOf<Song>()
    var songState = ""
    private var isForeground = false
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

            playNext()
        }
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {

    }

    override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {

    }

    override fun onPrepared(p0: MediaPlayer?) {

    }

    override fun onSeekComplete(p0: MediaPlayer?) {

    }

    private fun startSong() {

    }

    private fun playNextSong() {

    }

    private fun playPreviousSong() {

    }

    private fun setState(state: String) {
        songState = state
    }

    private fun showNotification(song: Song) {
        val notificationGenerator = NotificationG
    }

    inner class PlaybackBinder: Binder() {
        fun getService(): PlaybackService {
            return this@PlaybackService
        }
    }
}