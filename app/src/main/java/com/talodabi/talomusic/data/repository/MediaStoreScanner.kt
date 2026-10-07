package com.talodabi.talomusic.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.talodabi.talomusic.data.model.Album
import com.talodabi.talomusic.data.model.Artist
import com.talodabi.talomusic.data.model.Genre
import com.talodabi.talomusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreScanner(private val context: Context) {

    private val albumArtUriBase = Uri.parse("content://media/external/audio/albumart")

    suspend fun scanSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songList = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )

        // Only music tracks longer than 15 seconds (filter out notification sounds and ringtones)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf("15000")
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Track"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    val track = cursor.getInt(trackColumn)
                    val filePath = cursor.getString(dataColumn) ?: ""
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val artworkUri = ContentUris.withAppendedId(
                        albumArtUriBase,
                        albumId
                    ).toString()

                    songList.add(
                        Song(
                            id = id,
                            title = title.trim(),
                            artist = if (artist.contains("<unknown>", ignoreCase = true)) "Unknown Artist" else artist.trim(),
                            album = album.trim(),
                            albumArtist = null,
                            genre = null,
                            durationMs = duration,
                            artworkUri = artworkUri,
                            trackNumber = track,
                            contentUri = contentUri,
                            filePath = filePath,
                            dateAdded = dateAdded,
                            isFavorite = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        songList
    }

    fun groupAlbums(songs: List<Song>): List<Album> {
        return songs.groupBy { it.album }
            .mapNotNull { (albumName, albumSongs) ->
                val firstSong = albumSongs.firstOrNull() ?: return@mapNotNull null
                Album(
                    id = albumSongs.hashCode().toLong(),
                    title = albumName,
                    artist = firstSong.artist,
                    songCount = albumSongs.size,
                    artworkUri = firstSong.artworkUri,
                    year = 0
                )
            }.sortedBy { it.title.lowercase() }
    }

    fun groupArtists(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artist }
            .mapNotNull { (artistName, artistSongs) ->
                val albums = artistSongs.map { it.album }.distinct().size
                Artist(
                    id = artistName.hashCode().toLong(),
                    name = artistName,
                    songCount = artistSongs.size,
                    albumCount = albums
                )
            }.sortedBy { it.name.lowercase() }
    }

    fun groupGenres(songs: List<Song>): List<Genre> {
        return songs.groupBy { it.genre ?: "Various" }
            .mapNotNull { (genreName, genreSongs) ->
                Genre(
                    id = genreName.hashCode().toLong(),
                    name = genreName,
                    songCount = genreSongs.size
                )
            }.sortedBy { it.name.lowercase() }
    }
}
