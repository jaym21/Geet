package dev.jaym21.geet.utils

object Constants {

    const val MAIN_VIEW_PAGER_SIZE = 5

    //shared preferences
    const val PREFERENCES_HELPER = "preferences_helper"
    const val SONG_SORT_ORDER = "song_sort_order"
    const val QUEUE_IDS = "queue_ids"
    const val IS_REPEAT_ON = "is_repeat_on"

    //permissions
    const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 100
    const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 101

    //services
    const val SONG_LOADED = "song_loaded"
    const val SONG_STARTED = "song_started"
    const val SONG_PLAYED = "song_played"
    const val SONG_PAUSED = "song_paused"
    const val SONG_ENDED = "song_ended"
    const val IS_PLAYING = "is_playing"

    //notification
    const val NOTIFICATION_SONG_ID = 1
    const val NOTIFICATION_SONG_CHANNEL_ID = "notification_song_channel_id"
    const val FROM_NOTIFICATION = "from_notification"
    const val CURRENT_PLAYING_SONG_POSITION = "current_playing_song_position"
    const val NEXT_CLICKED = "next_clicked"
    const val PREVIOUS_CLICKED = "previous_clicked"
    const val PLAY_PAUSE_CLICKED = "play_pause_clicked"

    //player
    const val NO_SONG_ID =  -1L
    const val MAX_SHUFFLE__BUFFER_SIZE = 16

    //playback modes
    const val ALL_SONGS = "all_songs"
    const val IN_ALBUM = "in_album"
    const val IN_ARTIST = "in_artist"
}