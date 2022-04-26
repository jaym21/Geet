package dev.jaym21.geet.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.provider.Settings
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar

//SeekBar to be used with MediaSessionCompat
class MediaSeekBar: AppCompatSeekBar {

    private var mediaController: MediaControllerCompat? = null
    private var controllerCallback: ControllerCallback? = null
    private var isTracking = false
    private var progressAnimator: ValueAnimator? = null
    private val durationScale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

    private val onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
            isTracking = true
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
            mediaController?.transportControls?.seekTo(progress.toLong())
            isTracking = false
        }

    }

    //constructors
    constructor(context: Context) : super(context) {
        super.setOnSeekBarChangeListener(onSeekBarChangeListener)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        super.setOnSeekBarChangeListener(onSeekBarChangeListener)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        super.setOnSeekBarChangeListener(onSeekBarChangeListener)
    }

    //cannot add custom seek bar change listeners
    override fun setOnSeekBarChangeListener(l: SeekBar.OnSeekBarChangeListener) {
        throw UnsupportedOperationException("Cannot add listeners to a MediaSeekBar")
    }

    fun setMediaController(mediaControllerCompat: MediaControllerCompat?) {
        if (mediaControllerCompat != null) {
            controllerCallback = ControllerCallback()
            mediaControllerCompat.registerCallback(controllerCallback!!)
            controllerCallback?.onMetadataChanged(mediaControllerCompat.metadata)
            controllerCallback?.onPlaybackStateChanged(mediaControllerCompat.playbackState)
        } else if (mediaController != null) {
            mediaController?.registerCallback(controllerCallback!!)
            controllerCallback = null
        }
        mediaController = mediaControllerCompat
    }

    fun disconnectMediaController() {
        if (mediaController != null) {
            mediaController?.unregisterCallback(controllerCallback!!)
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
                val timeToEnd = (max - progress)

                if (timeToEnd > 0) {
                    progressAnimator?.cancel()
                    progressAnimator = ValueAnimator.ofInt(progress, max)
                        .setDuration((timeToEnd / durationScale).toLong())

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
            if (isTracking) {
                valueAnimator.cancel()
                return
            }

            val animatedIntValue = valueAnimator.animatedValue as Int
            progress = animatedIntValue
        }
    }
}