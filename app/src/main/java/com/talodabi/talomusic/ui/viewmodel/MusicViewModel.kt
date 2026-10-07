package com.talodabi.talomusic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talodabi.talomusic.data.model.Album
import com.talodabi.talomusic.data.model.Artist
import com.talodabi.talomusic.data.model.Genre
import com.talodabi.talomusic.data.model.Playlist
import com.talodabi.talomusic.data.model.Song
import com.talodabi.talomusic.data.repository.MusicRepository
import com.talodabi.talomusic.data.repository.SettingsRepository
import com.talodabi.talomusic.data.repository.ThemeMode
import com.talodabi.talomusic.playback.PlaybackConnection
import com.talodabi.talomusic.playback.PlaybackUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchResult(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList()
) {
    val isEmpty: Boolean
        get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty() && playlists.isEmpty()
}

class MusicViewModel(
    private val musicRepository: MusicRepository,
    private val playbackConnection: PlaybackConnection,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val allSongs: StateFlow<List<Song>> = musicRepository.allSongs
    val albums: StateFlow<List<Album>> = musicRepository.albums
    val artists: StateFlow<List<Artist>> = musicRepository.artists
    val genres: StateFlow<List<Genre>> = musicRepository.genres
    val favoriteSongs: StateFlow<List<Song>> = musicRepository.favoriteSongs
    val recentlyPlayed: StateFlow<List<Song>> = musicRepository.recentlyPlayed
    val playlists: StateFlow<List<Playlist>> = musicRepository.playlists

    val playbackState: StateFlow<PlaybackUiState> = playbackConnection.playbackState

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.DARK)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<SearchResult> = combine(
        searchQuery,
        allSongs,
        albums,
        artists,
        playlists
    ) { query, songs, albs, arts, plays ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            SearchResult()
        } else {
            SearchResult(
                songs = songs.filter { it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) },
                albums = albs.filter { it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) },
                artists = arts.filter { it.name.lowercase().contains(q) },
                playlists = plays.filter { it.name.lowercase().contains(q) }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        playbackConnection.playSong(song, queue)
        viewModelScope.launch {
            musicRepository.recordRecentPlay(song.id)
        }
    }

    fun togglePlayPause() = playbackConnection.togglePlayPause()
    fun skipToNext() = playbackConnection.skipToNext()
    fun skipToPrevious() = playbackConnection.skipToPrevious()
    fun seekTo(positionMs: Long) = playbackConnection.seekTo(positionMs)
    fun toggleShuffle() = playbackConnection.toggleShuffle()
    fun cycleRepeatMode() = playbackConnection.cycleRepeatMode()

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(songId)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            musicRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun refreshLibrary() {
        musicRepository.refresh()
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun getSongsForPlaylist(playlistId: Long): StateFlow<List<Song>> {
        return musicRepository.getSongsForPlaylist(playlistId)
    }
}
