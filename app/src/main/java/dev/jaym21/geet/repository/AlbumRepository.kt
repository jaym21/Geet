package dev.jaym21.geet.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import dev.jaym21.geet.extensions.*
import dev.jaym21.geet.extensions.getInt
import dev.jaym21.geet.extensions.getLong
import dev.jaym21.geet.extensions.getString
import dev.jaym21.geet.extensions.getStringOrNull
import dev.jaym21.geet.models.Album
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.models.Song

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

    fun getAlbum(id: Long): Album {
        val cursor= makeAlbumCursor("_id=?", arrayOf(id.toString()))
        var album = Album()
        if (cursor!= null && cursor.moveToFirst()) {
            album = getAlbumFromCursor(cursor)
        }
        cursor?.close()
        return album
    }

    fun searchAlbums(searchQuery: String, limit: Int): List<Album> {
        val cursor = makeAlbumCursor("album LIKE ?", arrayOf("$searchQuery%"))
        val albums = arrayListOf<Album>()

        if (cursor!= null && cursor.moveToFirst()) {
            do {
                albums.add(getAlbumFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()

        if (albums.size < limit) {
            val moreCursor = makeAlbumCursor("album LIKE ?", arrayOf("%_$searchQuery%"))
            val moreAlbums = arrayListOf<Album>()
            if (moreCursor!= null && moreCursor.moveToFirst()) {
                do {
                    moreAlbums.add(getAlbumFromCursor(moreCursor))
                } while (moreCursor.moveToNext())
            }
            cursor?.close()
            albums += moreAlbums
        }
        return if (albums.size < limit) {
            albums
        }  else {
            albums.subList(0, limit)
        }
    }

    fun getSongsForAlbum(caller: String?, albumId: Long): List<Song> {
        MediaID.currentCaller = caller
        val cursor = makeAlbumSongCursor(albumId)
        val songs = arrayListOf<Song>()

        if (cursor!= null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    fun getAlbumsForArtist(artistId: Long): List<Album> {
        //getting all albums and then filtering using artistId
        val allAlbums: List<Album> = getAllAlbums(null)
        val artistAlbums = ArrayList<Album>()

        for (album in allAlbums) {
            if (album.artistId == artistId) {
                artistAlbums.add(album)
            }
        }
        return artistAlbums
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

    private fun makeAlbumSongCursor(albumID: Long): Cursor? {
        val selection = "is_music=1 AND title != '' AND album_id=$albumID"
        return context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf("_id", "title", "artist", "album", "duration", "track", "album_id", "artist_id"),
            selection,
            null,
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