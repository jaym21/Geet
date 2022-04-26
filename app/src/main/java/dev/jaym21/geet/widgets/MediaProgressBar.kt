package dev.jaym21.geet.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.provider.Settings
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar

class MediaProgressBar : ProgressBar {

    private var mediaController: MediaControllerCompat? = null
    private var controllerCallback: ControllerCallback? = null
    private var progressAnimator: ValueAnimator? = null
    private val mDurationScale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

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
            setProgress(progress)

            if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                val timeToEnd = ((max - progress) / state.playbackSpeed).toInt()

                if (timeToEnd > 0) {
                    progressAnimator?.cancel()
                    progressAnimator = ValueAnimator.ofInt(progress, max)
                        .setDuration((timeToEnd / mDurationScale).toLong())
                    progressAnimator!!.interpolator = LinearInterpolator()
                    progressAnimator!!.addUpdateListener(this)
                    progressAnimator!!.start()
                }
            } else {
                setProgress(state.position.toInt())
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            val max = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)?.toInt() ?: 0

            setMax(max)
            onPlaybackStateChanged(mediaController?.playbackState)
        }

        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            //if user is sliding the seek bar then canceling the animation
            val animatedIntValue = valueAnimator.animatedValue as Int
            progress = animatedIntValue
        }
    }
}