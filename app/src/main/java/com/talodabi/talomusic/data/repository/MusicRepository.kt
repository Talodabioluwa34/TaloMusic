package com.talodabi.talomusic.data.repository

import com.talodabi.talomusic.data.local.dao.FavoriteDao
import com.talodabi.talomusic.data.local.dao.PlaylistDao
import com.talodabi.talomusic.data.local.dao.RecentPlayDao
import com.talodabi.talomusic.data.local.entity.FavoriteSongEntity
import com.talodabi.talomusic.data.local.entity.PlaylistEntity
import com.talodabi.talomusic.data.local.entity.PlaylistSongCrossRef
import com.talodabi.talomusic.data.local.entity.RecentPlayEntity
import com.talodabi.talomusic.data.model.Album
import com.talodabi.talomusic.data.model.Artist
import com.talodabi.talomusic.data.model.Genre
import com.talodabi.talomusic.data.model.Playlist
import com.talodabi.talomusic.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(
    private val scanner: MediaStoreScanner,
    private val favoriteDao: FavoriteDao,
    private val playlistDao: PlaylistDao,
    private val recentPlayDao: RecentPlayDao,
    private val scope: CoroutineScope
) {
    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    private val _favoriteIds = favoriteDao.getAllFavoriteSongIds()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    // All songs enriched with favorite flag
    val allSongs: StateFlow<List<Song>> = combine(_rawSongs, _favoriteIds) { songs, favIds ->
        val favSet = favIds.toSet()
        songs.map { song -> song.copy(isFavorite = favSet.contains(song.id)) }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val albums: StateFlow<List<Album>> = allSongs.map { songs ->
        scanner.groupAlbums(songs)
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val artists: StateFlow<List<Artist>> = allSongs.map { songs ->
        scanner.groupArtists(songs)
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val genres: StateFlow<List<Genre>> = allSongs.map { songs ->
        scanner.groupGenres(songs)
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val favoriteSongs: StateFlow<List<Song>> = allSongs.map { songs ->
        songs.filter { it.isFavorite }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = combine(allSongs, recentPlayDao.getRecentSongIds(50)) { songs, recentIds ->
        val songMap = songs.associateBy { it.id }
        recentIds.mapNotNull { songMap[it] }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val playlists: StateFlow<List<Playlist>> = playlistDao.getAllPlaylists().map { entities ->
        entities.map { entity ->
            Playlist(
                id = entity.id,
                name = entity.name,
                songCount = 0,
                createdAt = entity.createdAt
            )
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            val scanned = scanner.scanSongs()
            _rawSongs.value = scanned
        }
    }

    suspend fun toggleFavorite(songId: Long) = withContext(Dispatchers.IO) {
        val isFav = favoriteDao.isFavoriteSync(songId)
        if (isFav) {
            favoriteDao.removeFavorite(songId)
        } else {
            favoriteDao.addFavorite(FavoriteSongEntity(songId = songId))
        }
    }

    suspend fun recordRecentPlay(songId: Long) = withContext(Dispatchers.IO) {
        recentPlayDao.recordPlay(RecentPlayEntity(songId = songId, playedAt = System.currentTimeMillis()))
    }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name.trim()))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): StateFlow<List<Song>> {
        return combine(allSongs, playlistDao.getSongIdsForPlaylist(playlistId)) { songs, ids ->
            val songMap = songs.associateBy { it.id }
            ids.mapNotNull { songMap[it] }
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
