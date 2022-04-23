package dev.jaym21.geet.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import dev.jaym21.geet.extensions.getInt
import dev.jaym21.geet.extensions.getLong
import dev.jaym21.geet.extensions.getString
import dev.jaym21.geet.extensions.getStringOrNull
import dev.jaym21.geet.models.Album
import dev.jaym21.geet.models.MediaID

class AlbumRepository(private val context: Context) {

    fun getAllAlbums(caller: String?): List<Album> {
        if (caller != null)
            MediaID.currentCaller = caller

        val cursor = makeAlbumCursor(null, null)
        val albums = arrayListOf<Album>()

        if (cursor!= null && cursor.moveToFirst()) {
            do {
                albums.add(getAlbumFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()

        return albums
    }

    private fun makeAlbumCursor(selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        return context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            arrayOf("_id", "album", "artist", "artist_id", "numsongs", "minyear"),
            selection,
            paramArrayOfString,
            MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
        )
    }

    private fun getAlbumFromCursor(cursor: Cursor): Album {
        val id = cursor.getLong(MediaStore.Audio.AudioColumns._ID)
        val albumTitle = cursor.getString(MediaStore.Audio.AudioColumns.ALBUM)
        val artistName = cursor.getStringOrNull(MediaStore.Audio.AlbumColumns.ARTIST)
        val artistId = cursor.getLong(MediaStore.Audio.AudioColumns.ARTIST_ID)
        val noOfSongs = cursor.getInt(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS)
        val year = cursor.getInt(MediaStore.Audio.AlbumColumns.FIRST_YEAR)

        return Album(
            id,
            albumTitle,
            artistName ?: "",
            artistId,
            noOfSongs,
            year
        )
    }
}