package dev.jaym21.geet.playback.player

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import java.lang.IllegalStateException

typealias OnPrepared<T> = T.() -> Unit
typealias OnError<T> = T.(error: Throwable) -> Unit
typealias OnCompletion<T> = T.() -> Unit

//Wrapper interface around Media Player
class MusicPlayer(val context: Application): MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private var _player: MediaPlayer? = null
    private val player: MediaPlayer
        get() {
            if (_player == null) {
                _player = createPlayer(this)
            }
            return _player ?: throw IllegalStateException("Not possible")
        }

    private var wasPrepared = false
    private var onPrepared: OnPrepared<MusicPlayer> = {}
    private var onError: OnError<MusicPlayer> = {}
    private var onCompletion: OnCompletion<MusicPlayer> = {}

    fun setSource(uri: Uri): Boolean {
        try {
            player.setDataSource(context, uri)
        } catch (e: Exception) {
            onError(this, e)
            return false
        }
        return true
    }

    fun setSource(path: String): Boolean {
        try {
            player.setDataSource(path)
        } catch (e: Exception) {
            onError(this, e)
            return false
        }
        return true
    }

    fun prepare() {
        player.prepareAsync()
    }

    fun isPrepared() = wasPrepared

    fun isPlaying() = player.isPlaying

    fun play() {
        player.start()
    }

    fun pause() {
        player.pause()
    }

    fun stop() {
        player.stop()
    }

    fun reset() {
        player.reset()
    }

    fun release() {
        player.release()
        _player = null
    }

    fun seekTo(position: Int) {
        player.seekTo(position)
    }

    fun position() = player.currentPosition

    fun onPrepared(prepared: OnPrepared<MusicPlayer>) {
        this.onPrepared = prepared
    }

    fun onError(error: OnError<MusicPlayer>) {
        this.onError = error
    }

    fun onCompletion(completion: OnCompletion<MusicPlayer>) {
        this.onCompletion = completion
    }

    //MediaPlayer Callbacks
    override fun onPrepared(p0: MediaPlayer?) {
        wasPrepared = true
        onPrepared(this)
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        wasPrepared = false
        return false
    }

    override fun onCompletion(p0: MediaPlayer?) {
        onCompletion(this)
    }

    private fun createPlayer(musicPlayer: MusicPlayer): MediaPlayer {
        return MediaPlayer().apply {
            setWakeMode(musicPlayer.context, PowerManager.PARTIAL_WAKE_LOCK)
            val attributes = AudioAttributes.Builder().apply {
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                setUsage(AudioAttributes.USAGE_MEDIA)
            }.build()
            setAudioAttributes(attributes)
            setOnPreparedListener(musicPlayer)
            setOnCompletionListener(musicPlayer)
            setOnErrorListener(musicPlayer)
        }
    }
}

