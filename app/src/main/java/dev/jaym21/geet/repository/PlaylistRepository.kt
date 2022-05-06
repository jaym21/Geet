package dev.jaym21.geet.repository

import android.content.ContentProviderOperation
import android.content.Context
import android.content.OperationApplicationException
import android.database.Cursor
import android.os.RemoteException
import android.provider.MediaStore
import dev.jaym21.geet.extensions.*
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.models.Playlist
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.Constants
import java.lang.StringBuilder

class PlaylistRepository(private val context: Context) {

    fun getPlaylists(caller: String?): List<Playlist> {
        if (caller != null)
            MediaID.currentCaller = caller

        val cursor = makePlaylistCursor(null, null)
        val playlists = arrayListOf<Playlist>()

        if (cursor!= null && cursor.moveToFirst()) {
            do {
                playlists.add(getPlaylistFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()

        return playlists
    }

    fun getSongsInPlaylist(caller: String?, playlistId: Long): List<Song> {
        MediaID.currentCaller = caller
        val playlistCount = countPlaylist(playlistId)

        makePlaylistSongCursor(playlistId)?.use {
            var runCleanup = false
            if (it.count != playlistCount) {
                runCleanup = true
            }

            if (!runCleanup && it.moveToFirst()) {
                val playOrderCol = it.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.PLAY_ORDER)
                var lastPlayOrder = -1
                do {
                    val playOrder = it.getInt(playOrderCol)
                    if (playOrder == lastPlayOrder) {
                        runCleanup = true
                        break
                    }
                    lastPlayOrder = playOrder
                } while (it.moveToNext())
            }

            if (runCleanup) {
                cleanupPlaylist(playlistId, it, true)
            }
        }

        val cursor = makePlaylistSongCursor(playlistId)
        val songs = arrayListOf<Song>()

        if (cursor!= null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    private fun deletePlaylist(playlistId: Long): Int {
        val localUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        val localStringBuilder = StringBuilder().apply {
            append("_id IN (")
            append(playlistId)
            append(")")
        }
        return context.contentResolver.delete(localUri, localStringBuilder.toString(), null)
    }

    private fun getPlaylistFromCursor(cursor: Cursor): Playlist {
        val id = cursor.getLong(MediaStore.Audio.AudioColumns._ID)
        val playlistName = cursor.getStringOrNull("name")
        val noOfSongs = getSongCountForPlaylist(id)

        val songs = getSongsInPlaylist(MediaID.CALLER_SELF, id)
        val albumIds = mutableListOf<Long>()
        for (i in songs) {
            albumIds.add(i.albumId)
        }

        return Playlist(
            id,
            playlistName ?: "",
            noOfSongs,
            albumIds
        )
    }

    private fun countPlaylist(playlistId: Long): Int {
        return context.contentResolver.query(
            MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
            arrayOf(MediaStore.Audio.Playlists.Members.AUDIO_ID),
            null,
            null,
            MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER
        )?.use {
            if (it.moveToFirst()) {
                it.count
            } else {
                0
            }
        } ?: 0
    }

    private fun cleanupPlaylist(
        playlistId: Long,
        cursor: Cursor,
        closeCursorAfter: Boolean
    ) {
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID)
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        val ops = arrayListOf<ContentProviderOperation>().apply {
            add(ContentProviderOperation.newDelete(uri).build())
        }

        if (cursor.moveToFirst() && cursor.count > 0) {
            do {
                val builder = ContentProviderOperation.newInsert(uri)
                    .withValue(MediaStore.Audio.Playlists.Members.PLAY_ORDER, cursor.position)
                    .withValue(MediaStore.Audio.Playlists.Members.AUDIO_ID, cursor.getLong(idCol))
                if ((cursor.position + 1) % Constants.YIELD_FREQUENCY == 0) {
                    builder.withYieldAllowed(true)
                }
                ops.add(builder.build())
            } while (cursor.moveToNext())
        }

        try {
            context.contentResolver.applyBatch(MediaStore.AUTHORITY, ops)
        } catch (e: RemoteException) {
        } catch (e: OperationApplicationException) {
        }

        if (closeCursorAfter) {
            cursor.close()
        }
    }

    private fun getSongCountForPlaylist(playlistId: Long): Int {
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        return context.contentResolver.query(uri, arrayOf(MediaStore.Audio.AudioColumns._ID), "${MediaStore.Audio.AudioColumns.IS_MUSIC}=1 AND ${MediaStore.Audio.AudioColumns.TITLE} != ''", null, null)?.use {
            if (it.moveToFirst()) {
                it.count
            } else {
                0
            }
        } ?: 0
    }

    private fun makePlaylistCursor(selection: String?, paramArrayOfString: Array<String>?): Cursor? {
        return context.contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            arrayOf("_id", "artist", "number_of_albums", "number_of_tracks"),
            selection,
            paramArrayOfString,
            MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
        )
    }

    private fun makePlaylistSongCursor(artistId: Long): Cursor? {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "is_music=1 AND title != '' AND artist_id=$artistId"
        return context.contentResolver.query(
            uri,
            arrayOf("_id", "title", "artist", "album", "duration", "track", "album_id", "artist_id"),
            selection,
            null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
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