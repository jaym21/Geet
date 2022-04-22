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
interface MusicPlayer {

    fun setSource(uri: Uri): Boolean
    fun setSource(path: String): Boolean
    fun prepare()
    fun isPrepared(): Boolean
    fun isPlaying(): Boolean
    fun play()
    fun pause()
    fun stop()
    fun reset()
    fun release()
    fun seekTo(position: Int)
    fun position(): Int
    fun onPrepared(prepared: OnPrepared<MusicPlayer>)
    fun onError(error: OnError<MusicPlayer>)
    fun onCompletion(completion: OnCompletion<MusicPlayer>)
}

class IMusicPlayer(val context: Application): MusicPlayer, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

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

    override fun setSource(uri: Uri): Boolean {
        try {
            player.setDataSource(context, uri)
        } catch (e: Exception) {
            onError(this, e)
            return false
        }
        return true
    }

    override fun setSource(path: String): Boolean {
        try {
            player.setDataSource(path)
        } catch (e: Exception) {
            onError(this, e)
            return false
        }
        return true
    }

    override fun prepare() {
        player.prepareAsync()
    }

    override fun isPrepared() = wasPrepared

    override fun isPlaying() = player.isPlaying

    override fun play() {
        player.start()
    }

    override fun pause() {
        player.pause()
    }

    override fun stop() {
        player.stop()
    }

    override fun reset() {
        player.reset()
    }

    override fun release() {
        player.release()
        _player = null
    }

    override fun seekTo(position: Int) {
        player.seekTo(position)
    }

    override fun position() = player.currentPosition

    override fun onPrepared(prepared: OnPrepared<MusicPlayer>) {
        this.onPrepared = prepared
    }

    override fun onError(error: OnError<MusicPlayer>) {
        this.onError = error
    }

    override fun onCompletion(completion: OnCompletion<MusicPlayer>) {
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
}

private fun createPlayer(musicPlayer: IMusicPlayer): MediaPlayer {
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