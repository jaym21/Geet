package dev.jaym21.geet.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import dev.jaym21.geet.R
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.ui.MainActivity

class NotificationGenerator {

    private var currentSongPosition = 0
    private var isPlaying = true
    private lateinit var notificationBuilder: NotificationCompat.Builder
    lateinit var notificationManager: NotificationManager
    lateinit var remoteReviews: RemoteViews
    private lateinit var bigRemoteReviews: RemoteViews

    fun showSongNotification(context: Context, songPosition: Int, queuedSongs: List<Song>, bitmap: Bitmap?) {
        currentSongPosition = songPosition

        remoteReviews = RemoteViews(context.packageName, R.layout.song_notification)
        bigRemoteReviews = RemoteViews(context.packageName, R.layout.song_notification_expand)

        remoteReviews.setTextViewText(R.id.tvSongTitleNotification, queuedSongs[songPosition].title)
        remoteReviews.setTextViewText(R.id.tvArtistNameNotification, queuedSongs[songPosition].artist)

        if (bitmap != null) {
            remoteReviews.setImageViewBitmap(R.id.ivSongImageNotification, bitmap)
        } else {
            //TODO: set as app icon
            remoteReviews.setImageViewResource(R.id.ivSongImageNotification, R.drawable.ic_launcher_foreground)
        }

        bigRemoteReviews.setTextViewText(R.id.tvSongTitleNotificationExpand, queuedSongs[songPosition].title)
        bigRemoteReviews.setTextViewText(R.id.tvArtistNameNotificationExpand, queuedSongs[songPosition].artist)

        if (bitmap != null) {
            remoteReviews.setImageViewBitmap(R.id.ivSongImageNotificationExpand, bitmap)
        } else {
            //TODO: set as app icon
            remoteReviews.setImageViewResource(R.id.ivSongImageNotificationExpand, R.drawable.ic_launcher_foreground)
        }

        notificationBuilder = NotificationCompat.Builder(context.applicationContext, Constants.NOTIFICATION_SONG_CHANNEL_ID)
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.putExtra(Constants.FROM_NOTIFICATION, true)
        notificationIntent.putExtra(Constants.IS_PLAYING, isPlaying)
        notificationIntent.putExtra(Constants.CURRENT_PLAYING_SONG_POSITION, currentSongPosition)

        val pendingIntent =  PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder.setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Geet")
            .setContentText("Song Notification")
            .setContent(remoteReviews)
            .setCustomBigContentView(bigRemoteReviews)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val nextClick = Intent(Constants.NEXT_CLICKED)
        val previousClick = Intent(Constants.PREVIOUS_CLICKED)
        val playPauseClick = Intent(Constants.PLAY_PAUSE_CLICKED)

        val nextClickPendingIntent = PendingIntent.getBroadcast(context, 0, nextClick, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteReviews.setOnClickPendingIntent(R.id.ivNextNotification, nextClickPendingIntent)
        bigRemoteReviews.setOnClickPendingIntent(R.id.ivNextNotificationExpand, nextClickPendingIntent)

        val previousClickPendingIntent = PendingIntent.getBroadcast(context, 0, previousClick, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteReviews.setOnClickPendingIntent(R.id.ivPreviousNotification, previousClickPendingIntent)
        bigRemoteReviews.setOnClickPendingIntent(R.id.ivPreviousNotificationExpand, previousClickPendingIntent)

        val playPauseClickPendingIntent = PendingIntent.getBroadcast(context, 0, playPauseClick, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteReviews.setOnClickPendingIntent(R.id.ivPlayNotification, playPauseClickPendingIntent)
        bigRemoteReviews.setOnClickPendingIntent(R.id.ivPlayNotificationExpand, playPauseClickPendingIntent)

        notificationBuilder.setOngoing(true)
        notificationManager.notify(Constants.NOTIFICATION_SONG_ID, notificationBuilder.build())
    }

    fun updateView(isPlaying: Boolean, currentSongPosition: Int) {
        this.isPlaying = isPlaying
        this.currentSongPosition = currentSongPosition

        if (isPlaying) {
            remoteReviews.setImageViewResource(R.id.ivPlayNotification, R.drawable.ic_pause)
            bigRemoteReviews.setImageViewResource(R.id.ivPlayNotificationExpand, R.drawable.ic_pause)
            notificationBuilder.setOngoing(true)
        } else {
            notificationBuilder.setOngoing(true)
            remoteReviews.setImageViewResource(R.id.ivPlayNotification, R.drawable.ic_play)
            bigRemoteReviews.setImageViewResource(R.id.ivPlayNotificationExpand, R.drawable.ic_play)
        }
        notificationBuilder.setContent(remoteReviews)
        notificationBuilder.setCustomBigContentView(bigRemoteReviews)
        notificationManager.notify(Constants.NOTIFICATION_SONG_ID, notificationBuilder.build())
    }
}