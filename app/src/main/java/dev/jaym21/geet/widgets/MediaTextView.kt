package dev.jaym21.geet.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.provider.Settings
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatTextView
import dev.jaym21.geet.utils.SongUtils

class MediaTextView : AppCompatTextView {

    private var mediaController: MediaControllerCompat? = null
    private var controllerCallback: MediaTextView.ControllerCallback? = null
    private var duration: Int = 0
    private var progressAnimator: ValueAnimator? = null
    private val durationScale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setMediaController(mediaControllerCompat: MediaControllerCompat?) {
        if (mediaControllerCompat != null) {
            controllerCallback = ControllerCallback()
            mediaControllerCompat.registerCallback(controllerCallback!!)
            controllerCallback!!.onMetadataChanged(mediaControllerCompat.metadata)
            controllerCallback!!.onPlaybackStateChanged(mediaControllerCompat.playbackState)
        } else if (mediaController != null) {
            mediaController!!.unregisterCallback(controllerCallback!!)
            controllerCallback = null
        }

        mediaController = mediaControllerCompat
    }

    fun disconnectMediaController() {
        if (mediaController != null) {
            mediaController!!.unregisterCallback(controllerCallback!!)
            controllerCallback = null
            mediaController = null
        }
    }

    private inner class ControllerCallback : MediaControllerCompat.Callback(), ValueAnimator.AnimatorUpdateListener {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            state ?: return

            //if an animation is ongoing then stopping it
            if (progressAnimator != null) {
                progressAnimator!!.cancel()
                progressAnimator = null
            }

            val progress = state.position.toInt()

            text = SongUtils.formatTimeStringShort((progress / 1000).toLong())

            if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                val timeToEnd = ((duration - progress) / state.playbackSpeed).toInt()

                if (timeToEnd > 0) {
                    progressAnimator?.cancel()
                    progressAnimator = ValueAnimator.ofInt(progress, duration)
                        .setDuration((timeToEnd / durationScale).toLong())
                    progressAnimator!!.interpolator = LinearInterpolator()
                    progressAnimator!!.addUpdateListener(this)
                    progressAnimator!!.start()
                }
            } else {

                text = SongUtils.formatTimeStringShort(state.position / 1000)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            val max = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)?.toInt() ?: 0
            duration = max
            onPlaybackStateChanged(mediaController?.playbackState)
        }

        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val animatedIntValue = valueAnimator.animatedValue as Int
            text = SongUtils.formatTimeStringShort((animatedIntValue / 1000).toLong())
        }
    }
}