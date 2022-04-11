package dev.jaym21.geet.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import dev.jaym21.geet.extensions.getInt
import dev.jaym21.geet.extensions.getLong
import dev.jaym21.geet.extensions.getString
import dev.jaym21.geet.extensions.getStringOrNull
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.PreferencesHelper

interface ISongsRepository {

    fun getSongs(): List<Song>
}

class SongsRepository(private val contentResolver: ContentResolver, private val context: Context): ISongsRepository {

    override fun getSongs(): List<Song> {
        val cursor = makeSongCursor(null, null)
        val songs = arrayListOf<Song>()
        if (cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return songs
    }


    private fun makeSongCursor(selection: String?, paramArrayOfString: Array<String>?): Cursor {
        return makeSongCursor(selection, paramArrayOfString, PreferencesHelper.getSongSortOrder(context))
    }

    private fun makeSongCursor(
        selection: String?,
        paramArrayOfString: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val selectionStatement = StringBuilder("is_music=1 AND title != ''")

        if (!selection.isNullOrEmpty()) {
            selectionStatement.append(" AND $selection")
        }

        val projection =
            arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id")

        return contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selectionStatement.toString(),
            paramArrayOfString,
            sortOrder
        ) ?: throw IllegalStateException("Unable to query, null returned")
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
