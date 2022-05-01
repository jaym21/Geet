package dev.jaym21.geet.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import dev.jaym21.geet.extensions.*
import dev.jaym21.geet.models.Artist
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.models.Song

class ArtistRepository(private val context: Context) {

    fun getAllArtists(caller: String?): List<Artist> {
        if (caller != null)
            MediaID.currentCaller = caller

        val cursor = makeArtistCursor(null, null)
        val artists = arrayListOf<Artist>()

        if (cursor!= null && cursor.moveToFirst()) {
            do {
                artists.add(getArtistFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()

        return artists
    }


    fun getSongsForArtist(caller: String?, artistId: Long): List<Song> {
        MediaID.currentCaller = caller
        val cursor = makeArtistSongCursor(artistId)
        val songs = arrayListOf<Song>()

        if (cursor!= null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    private fun makeArtistCursor(selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        return context.contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            arrayOf("_id", "artist", "number_of_albums", "number_of_tracks"),
            selection,
            paramArrayOfString,
            MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
        )
    }

    private fun makeArtistSongCursor(artistId: Long): Cursor? {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "is_music=1 AND title != '' AND artist_id=$artistId"
        return context.contentResolver.query(
            uri,
            arrayOf("_id", "title", "artist", "album", "duration", "track", "album_id"),
            selection,
            null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        )
    }

    private fun getArtistFromCursor(cursor: Cursor): Artist {
        val id = cursor.getLong(MediaStore.Audio.AudioColumns._ID)
        val artistName = cursor.getStringOrNull(MediaStore.Audio.AlbumColumns.ARTIST)
        val noOfSongs = cursor.getInt(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS)
        val noOfAlbums = cursor.getInt(MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS)

        val songs = getSongsForArtist(MediaID.CALLER_SELF, id)

        val albumsIds = mutableListOf<Long>()

        for (i in songs) {
            albumsIds.add(i.albumId)
        }

        return Artist(
            id,
            artistName ?: "",
            noOfSongs,
            noOfAlbums,
            albumsIds
        )
    }

    private fun getSongFromCursor(cursor: Cursor): Song {
        val id = cursor.getLong(MediaStore.Audio.AudioColumns._ID)
        val title = cursor.getString(MediaStore.Audio.AudioColumns.TITLE)
        val artistName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ARTIST)
        val albumName = cursor.getStringOrNull(MediaStore.Audio.AudioColumns.ALBUM)
        val duration = cursor.getLong(MediaStore.Audio.AudioColumns.DURATION)
        val albumId = cursor.getLong(MediaStore.Audio.AudioColumns.ALBUM_ID)
        val trackNumber = cursor.getInt(MediaStore.Audio.AudioColumns.TRACK).normalizeTrackNumber()

        return Song(
            id,
            title,
            artistName ?: "",
            albumName ?: "",
            duration,
            albumId,
            trackNumber
        )
    }
}