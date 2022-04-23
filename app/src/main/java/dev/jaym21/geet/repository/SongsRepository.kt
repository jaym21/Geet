package dev.jaym21.geet.repository


import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import dev.jaym21.geet.extensions.*
import dev.jaym21.geet.extensions.getInt
import dev.jaym21.geet.extensions.getLong
import dev.jaym21.geet.extensions.getString
import dev.jaym21.geet.extensions.getStringOrNull
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.models.Song

//TODO: Add sortOrder changing implementation
class SongsRepository(private val context: Context) {

    fun getSongs(caller: String?): List<Song> {
        MediaID.currentCaller = caller
        val cursor = makeSongCursor(null, null)
        val songs = arrayListOf<Song>()

        if (cursor!= null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    fun getSongForId(id: Long): Song {
        val cursor = makeSongCursor("_id = $id", null)
        var song = Song()
        if (cursor!= null && cursor.moveToFirst()) {
            song = getSongFromCursor(cursor)
        }
        cursor?.close()
        return song
    }

    fun getSongsForIds(ids: LongArray): List<Song> {
        var selection = "_id IN ("
        for (id in ids) {
            selection += "$id,"
        }

        if (ids.isNotEmpty()) {
            selection = selection.substring(0, selection.length - 1)
        }
        selection += ")"

        val cursor = makeSongCursor(selection, null)
        val songs = arrayListOf<Song>()

        if (cursor!= null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    fun searchSongs(searchQuery: String, limit: Int): List<Song> {
        val cursor = makeSongCursor("title LIKE ?", arrayOf("$searchQuery%"))
        val songs = arrayListOf<Song>()
        if (cursor!= null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()

        if (songs.size < limit) {
            val moreCursor = makeSongCursor("title LIKE ?", arrayOf("%_$searchQuery%"))
            val moreSongs = arrayListOf<Song>()
            if (moreCursor!= null && moreCursor.moveToFirst()) {
                do {
                    moreSongs.add(getSongFromCursor(moreCursor))
                } while (moreCursor.moveToNext())
            }
            cursor?.close()
            songs += moreSongs
        }
        return if (songs.size < limit) {
            songs
        }  else {
            songs.subList(0, limit)
        }
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
            selection,
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
        val trackNumber = cursor.getInt(MediaStore.Audio.AudioColumns.TRACK).normalizeTrackNumber()

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
