package dev.jaym21.geet.repository

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.content.Context
import android.content.OperationApplicationException
import android.database.Cursor
import android.os.RemoteException
import android.provider.BaseColumns
import android.provider.MediaStore
import dev.jaym21.geet.extensions.*
import dev.jaym21.geet.extensions.getInt
import dev.jaym21.geet.extensions.getLong
import dev.jaym21.geet.extensions.getStringOrNull
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.models.Playlist
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.Constants

class PlaylistRepository(private val context: Context) {

    fun createPlaylist(name: String?): Long {
        if (name.isNullOrEmpty()) {
            return -1
        }
        val projection = arrayOf(MediaStore.Audio.PlaylistsColumns.NAME)
        val selection = "${MediaStore.Audio.PlaylistsColumns.NAME} = ?"
        val selectionArgs = arrayOf(name)

        return context.contentResolver.query(
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use {
            return if (it.count <= 0) {
                val values = ContentValues(1).apply {
                    put(MediaStore.Audio.PlaylistsColumns.NAME, name)
                }
                context.contentResolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values)?.lastPathSegment?.toLong() ?: -1
            } else {
                -1
            }
        } ?: throw IllegalStateException("Unable to query ${MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI}, system returned null.")
    }

    fun getAllPlaylists(caller: String?): List<Playlist> {
        if (caller != null)
            MediaID.currentCaller = caller

        val cursor = makePlaylistCursor()
        val playlists = arrayListOf<Playlist>()

        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlists.add(getPlaylistFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor?.close()

        return playlists
    }

    fun addToPlaylist(playlistId: Long, ids: LongArray): Int {
        val projection = arrayOf("max(${MediaStore.Audio.Playlists.Members.PLAY_ORDER})")
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)

        val base: Int = context.contentResolver.query(uri, projection, null, null, null)?.use {
            if (it.moveToFirst()) {
                it.getInt(0) + 1
            } else {
                0
            }
        } ?: throw IllegalStateException("Unable to query $uri, system returned null.")

        var numInserted = 0
        var offset = 0
        while (offset < ids.size) {
            val bulkValues = makeInsertItems(ids, offset, 1000, base)
            numInserted += context.contentResolver.bulkInsert(uri, bulkValues)
            offset += 1000
        }

        return numInserted
    }

    private fun makePlaylistCursor(): Cursor? {
        return context.contentResolver.query(
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
            arrayOf(BaseColumns._ID, MediaStore.Audio.PlaylistsColumns.NAME),
            null,
            null,
            MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER
        )
    }

    private fun getPlaylistFromCursor(cursor: Cursor): Playlist {
        val id = cursor.getLong(MediaStore.Audio.Playlists._ID)
        val name = cursor.getStringOrNull(MediaStore.Audio.Playlists.NAME)
        val noOfSongs = getSongCountForPlaylist(id)

        val songs = getSongsInPlaylist(MediaID.CALLER_SELF, id)
        val albumsIds = mutableListOf<Long>()
        for (i in songs) {
            albumsIds.add(i.albumId)
        }

        return Playlist(
            id,
            name ?: "",
            noOfSongs,
            albumsIds
        )
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

    fun deletePlaylist(playlistId: Long): Int {
        val localUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        val localStringBuilder = StringBuilder().apply {
            append("_id IN (")
            append(playlistId)
            append(")")
        }
        return context.contentResolver.delete(localUri, localStringBuilder.toString(), null)
    }

    fun deleteTrackFromPlaylist(songId: Long, playlistId: Long) {
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        context.contentResolver.delete(
            uri,
            "${MediaStore.Audio.Playlists.Members.AUDIO_ID} = ?",
            arrayOf(songId.toString())
        )
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

    private fun makePlaylistSongCursor(playlistId: Long): Cursor? {
        val selection = StringBuilder().apply {
            append("${MediaStore.Audio.AudioColumns.IS_MUSIC}=1")
            append(" AND ${MediaStore.Audio.AudioColumns.TITLE} != ''")
        }
        val projection = arrayOf(
            MediaStore.Audio.Playlists.Members._ID,
            MediaStore.Audio.Playlists.Members.AUDIO_ID,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.ARTIST_ID,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.Playlists.Members.PLAY_ORDER
        )
        return context.contentResolver.query(
            MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
            projection,
            selection.toString(),
            null,
            MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER
        )
    }

    private fun getSongCountForPlaylist(playlistId: Long): Int {
        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
        return context.contentResolver.query(uri, arrayOf(MediaStore.Audio.Playlists._ID), "${MediaStore.Audio.AudioColumns.IS_MUSIC}=1 AND ${MediaStore.Audio.AudioColumns.TITLE} != ''", null, null)?.use {
            if (it.moveToFirst()) {
                it.count
            } else {
                0
            }
        } ?: 0
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

    private fun makeInsertItems(
        ids: LongArray,
        offset: Int,
        len: Int,
        base: Int
    ): Array<ContentValues> {
        var actualLen = len
        if (offset + actualLen > ids.size) {
            actualLen = ids.size - offset
        }
        val contentValuesList = mutableListOf<ContentValues>()
        for (i in 0 until actualLen) {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i)
                put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i])
            }
            contentValuesList.add(values)
        }
        return contentValuesList.toTypedArray()
    }
}