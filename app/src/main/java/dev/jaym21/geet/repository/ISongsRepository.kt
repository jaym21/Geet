package dev.jaym21.geet.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import dev.jaym21.geet.extensions.getInt
import dev.jaym21.geet.extensions.getLong
import dev.jaym21.geet.extensions.getString
import dev.jaym21.geet.extensions.getStringOrNull
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.PreferencesHelper

interface ISongsRepository {

    suspend fun getSongs(): List<Song>
}

class SongsRepository(private val context: Context): ISongsRepository {

    override suspend fun getSongs(): List<Song> {
        val cursor = makeSongCursor(null, null)
        val songs = arrayListOf<Song>()
        Log.d("TAGYOYO", "getSongs: ${cursor?.count}")
        if (cursor!= null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }


    private fun makeSongCursor(selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        return makeSongCursor(selection, paramArrayOfString, MediaStore.Audio.Media.DEFAULT_SORT_ORDER)
    }

    private fun makeSongCursor(
        selection: String?,
        paramArrayOfString: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val selectionStatement = StringBuilder(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''")

        if (!selection.isNullOrEmpty()) {
            selectionStatement.append(" AND $selection")
        }

        val projection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.AudioColumns.ARTIST_ID,
            MediaStore.Audio.AudioColumns.ALBUM_ID,)

        return context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
        )
    }

    private fun getSongFromCursor(cursor: Cursor): Song {
        val id = cursor.getLong(MediaStore.Audio.AudioColumns._ID)
        val title = cursor.getString(MediaStore.Audio.AudioColumns.TITLE)
        val artistName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ARTIST)
        val albumName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ALBUM)
        val duration = cursor.getLong(MediaStore.Audio.AudioColumns.DURATION)
        val albumId = cursor.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val artistId = cursor.getLong(MediaStore.Audio.AudioColumns.ARTIST_ID)
        val trackNumber = cursor.getInt(MediaStore.Audio.AudioColumns.TRACK)

        return Song(
            id,
            title,
            artistName ?: "",
            albumName ?: "",
            duration,
            albumId,
            artistId,
            trackNumber
        )
    }
}
