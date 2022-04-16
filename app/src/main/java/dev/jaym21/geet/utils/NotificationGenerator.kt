package dev.jaym21.geet.utils

import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import dev.jaym21.geet.R
import dev.jaym21.geet.models.Song

class NotificationGenerator {

    private var currentSongPosition = 0
    private var isPlaying = true
    lateinit var remoteReviews: RemoteViews
    lateinit var bigRemoteReviews: RemoteViews

    private fun showSongNotification(context: Context, songPosition: Int, queuedSongs: List<Song>, bitmap: Bitmap?) {
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
    }
}