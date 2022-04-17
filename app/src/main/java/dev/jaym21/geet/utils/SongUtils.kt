package dev.jaym21.geet.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dev.jaym21.geet.R
import java.io.FileNotFoundException

object SongUtils {

    fun getSongUri(id: Long): Uri {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    }

    fun getAlbumArtUri(albumId: Long): Uri {
        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        return ContentUris.withAppendedId(artworkUri, albumId)
    }

    fun getAlbumArtBitmap(context: Context, albumId: Long?): Bitmap? {
        if (albumId == null)
            return null

        return try {
            if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, getAlbumArtUri(albumId))
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, getAlbumArtUri(albumId))
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: FileNotFoundException) {
            //TODO: set as app icon
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground)
        }
    }
}