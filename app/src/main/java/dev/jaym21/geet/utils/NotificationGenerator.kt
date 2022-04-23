package dev.jaym21.geet.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.IdRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import androidx.palette.graphics.Palette
import dev.jaym21.geet.R
import dev.jaym21.geet.playback.PlaybackService
import dev.jaym21.geet.ui.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationGenerator(private val context: Application) {

    private var whenTime = -1L
    private lateinit var notificationManager: NotificationManager

    fun updateNotification(mediaSession: MediaSessionCompat) {
        GlobalScope.launch {
            notificationManager.notify(Constants.NOTIFICATION_SONG_ID, generateNotification(mediaSession))
        }
    }

    fun generateNotification(mediaSession: MediaSessionCompat): Notification {
        if (mediaSession.controller.metadata == null || mediaSession.controller.playbackState == null) {
            return emptyNotification()
        }

        val isPlaying = mediaSession.controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING
        val title = mediaSession.controller.metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        val artistName = mediaSession.controller.metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        val albumName = mediaSession.controller.metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
        val artwork = mediaSession.controller.metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)

        //setting play or pause button drawable icon according to isPlaying from mediaSession
        val playPauseButton = if (isPlaying) {
            R.drawable.ic_pause
        }else {
            R.drawable.ic_play
        }

        //pending intent for notification card click
        val nowPlayingIntent = Intent(context, MainActivity::class.java)
        val notificationClickIntent = PendingIntent.getActivity(context, 0, nowPlayingIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (whenTime == -1L)
            whenTime = System.currentTimeMillis()

        createNotificationChannel()

        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowCancelButton(true)
            .setShowActionsInCompactView(0, 1, 2)
            .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))

        //TODO: change icon
        val notificationBuilder = NotificationCompat.Builder(context, Constants.NOTIFICATION_SONG_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(artwork)
            .setStyle(style)
            .setContentIntent(notificationClickIntent)
            .setContentTitle(title)
            .setContentText(artistName)
            .setSubText(albumName)
            .setColorized(true)
            .setShowWhen(false)
            .setWhen(whenTime)
            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
            .addAction(previousAction(context))
            .addAction(playPauseAction(context, playPauseButton))
            .addAction(nextAction(context))

        //getting color palette using artwork
        if (artwork != null) {
            notificationBuilder.color = Palette.from(artwork)
                .generate()
                .getVibrantColor(ContextCompat.getColor(context, R.color.notification_vibrant))
        }

        return notificationBuilder.build()
    }

    private fun previousAction(context: Context): NotificationCompat.Action {
        val actionIntent = Intent(context, PlaybackService::class.java).apply {
            action = Constants.ACTION_NOTIFICATION_PREVIOUS
        }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, 0)
        return NotificationCompat.Action(R.drawable.ic_skip_previous, "", pendingIntent)
    }

    private fun playPauseAction(context: Context, @IdRes playPauseButton: Int): NotificationCompat.Action {
        val actionIntent = Intent(context, PlaybackService::class.java).apply {
            action = Constants.ACTION_NOTIFICATION_PLAY_PAUSE
        }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, 0)
        return NotificationCompat.Action(playPauseButton, "", pendingIntent)
    }

    private fun nextAction(context: Context): NotificationCompat.Action {
        val actionIntent = Intent(context, PlaybackService::class.java).apply {
            action = Constants.ACTION_NOTIFICATION_NEXT
        }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, 0)
        return NotificationCompat.Action(R.drawable.ic_skip_next, "", pendingIntent)
    }

    private fun emptyNotification(): Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(context, Constants.NOTIFICATION_SONG_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle("Geet")
            setColorized(true)
            setShowWhen(false)
            setWhen(whenTime)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O)
            return
        val name = context.getString(R.string.song_playback)
        val channel = NotificationChannel(Constants.NOTIFICATION_SONG_CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW).apply {
            description = context.getString(R.string.song_playback_controls)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }
}