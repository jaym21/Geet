package dev.jaym21.geet.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Song(
    var id: Long = 0,
    var title: String = "",
    var artist: String = "",
    var album: String = "",
    var duration: Long = 0,
    var albumId: Long = 0,
    var artistId: Long = 0,
    var trackNumber: Int = 0
): Parcelable
