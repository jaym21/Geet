package dev.jaym21.geet.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat


//to check if the headphones are unplugged
class NoisyCheckReceiver(private val context: Context, sessionToken: MediaSessionCompat.Token): BroadcastReceiver() {

    private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val controller = MediaControllerCompat(context, sessionToken)
    private var isRegistered = false

    fun register() {
        if (!isRegistered) {
            context.registerReceiver(this, noisyIntentFilter)
            isRegistered = true
        }
    }

    fun unRegister() {
        if (isRegistered) {
            context.unregisterReceiver(this)
            isRegistered = false
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY){
            controller.transportControls.pause()
        }
    }
}