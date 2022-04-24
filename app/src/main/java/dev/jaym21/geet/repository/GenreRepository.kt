package dev.jaym21.geet.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import dev.jaym21.geet.extensions.*
import dev.jaym21.geet.extensions.getInt
import dev.jaym21.geet.extensions.getLong
import dev.jaym21.geet.extensions.getString
import dev.jaym21.geet.extensions.getStringOrNull
import dev.jaym21.geet.models.Genre
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.models.Song

class GenreRepository(private val context: Context) {

    fun getAllGenres(caller: String?): List<Genre> {
        MediaID.currentCaller = caller

        val cursor = makeGenreCursor()
        val genres = arrayListOf<Genre>()

        if (cursor!= null && cursor.moveToFirst()) {
            do {
                genres.add(getGenreFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()

        return genres
    }

    fun getSongsForGenre(genreId: Long, caller: String?): List<Song> {
        MediaID.currentCaller = caller
        val cursor = makeGenreSongCursor(genreId)
        val songs = arrayListOf<Song>()

        if (cursor!= null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    private fun makeGenreCursor(): Cursor? {
        val uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME)
        return context.contentResolver.query(uri, projection, null, null, MediaStore.Audio.Genres.NAME)
    }

    private fun makeGenreSongCursor(genreID: Long): Cursor? {
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreID)
        val projection = arrayOf("_id", "title", "artist", "album", "duration", "track", "album_id", "artist_id")
        return context.contentResolver.query(uri, projection, null, null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        )
    }

    private fun getGenreFromCursor(cursor: Cursor): Genre {
        val id = cursor.getLong(MediaStore.Audio.AudioColumns._ID)
        val genreName = cursor.getStringOrNull(MediaStore.Audio.GenresColumns.NAME)
        val noOfSongs = getSongCountForGenre(id)

        return Genre(
            id,
            genreName ?: "",
            noOfSongs,
        )
    }

    private fun getSongCountForGenre(genreID: Long): Int {
        val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreID)
        return context.contentResolver.query(uri, null, null, null, null)?.use {
            it.moveToFirst()
            if (it.count == 0) {
                -1
            } else {
                it.count
            }
        } ?: -1
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