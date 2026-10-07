package com.talodabi.talomusic.ui.screens.musiclab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talodabi.talomusic.data.local.entity.ChordEventEntity
import com.talodabi.talomusic.data.local.entity.LoopPresetEntity
import com.talodabi.talomusic.data.local.entity.SongAnalysisEntity
import com.talodabi.talomusic.data.local.entity.SongSectionEntity
import com.talodabi.talomusic.data.model.Song
import com.talodabi.talomusic.data.music.ChordQuality
import com.talodabi.talomusic.data.music.MusicChord
import com.talodabi.talomusic.data.repository.AnalysisRepository
import com.talodabi.talomusic.data.repository.MusicRepository
import com.talodabi.talomusic.playback.PlaybackConnection
import com.talodabi.talomusic.playback.PlaybackUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NotationMode {
    CHORDS, ROMAN_NUMERALS, NASHVILLE_NUMBERS
}

class MusicLabViewModel(
    private val musicRepository: MusicRepository,
    private val analysisRepository: AnalysisRepository,
    private val playbackConnection: PlaybackConnection
) : ViewModel() {

    private val _currentSongId = MutableStateFlow<Long?>(null)

    val playbackState: StateFlow<PlaybackUiState> = playbackConnection.playbackState

    val currentSong: StateFlow<Song?> = _currentSongId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else {
            musicRepository.allSongs.flatMapLatest { songs ->
                flowOf(songs.firstOrNull { it.id == id })
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val songAnalysis: StateFlow<SongAnalysisEntity?> = _currentSongId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else analysisRepository.getAnalysis(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chords: StateFlow<List<ChordEventEntity>> = _currentSongId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else analysisRepository.getChords(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sections: StateFlow<List<SongSectionEntity>> = _currentSongId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else analysisRepository.getSections(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _transposeOffset = MutableStateFlow(0)
    val transposeOffset: StateFlow<Int> = _transposeOffset.asStateFlow()

    private val _notationMode = MutableStateFlow(NotationMode.CHORDS)
    val notationMode: StateFlow<NotationMode> = _notationMode.asStateFlow()

    fun loadSong(songId: Long) {
        _currentSongId.value = songId
        viewModelScope.launch {
            val song = musicRepository.allSongs.value.firstOrNull { it.id == songId }
            if (song != null) {
                analysisRepository.ensureAnalysis(song)
                // If not currently playing this song, queue and play it
                if (playbackState.value.currentSong?.id != song.id) {
                    playbackConnection.playSong(song, listOf(song))
                }
            }
        }
    }

    fun togglePlayPause() = playbackConnection.togglePlayPause()
    fun seekTo(positionMs: Long) = playbackConnection.seekTo(positionMs)
    fun setPlaybackSpeed(speed: Float) = playbackConnection.setPlaybackSpeed(speed)

    fun setLoopRange(startMs: Long, endMs: Long) {
        val sId = _currentSongId.value ?: return
        playbackConnection.setLoop(startMs, endMs)
        viewModelScope.launch {
            analysisRepository.saveLoop(sId, startMs, endMs, true)
        }
    }

    fun toggleLoop() = playbackConnection.toggleLoop()

    fun clearLoop() {
        val sId = _currentSongId.value ?: return
        playbackConnection.clearLoop()
        viewModelScope.launch {
            analysisRepository.saveLoop(sId, 0L, 0L, false)
        }
    }

    fun transpose(semitonesDelta: Int) {
        _transposeOffset.value = Math.floorMod(_transposeOffset.value + semitonesDelta + 12, 24) - 12
    }

    fun setNotationMode(mode: NotationMode) {
        _notationMode.value = mode
    }

    fun updateKeyAndBpm(tonic: Int, mode: String, bpm: Int) {
        val sId = _currentSongId.value ?: return
        viewModelScope.launch {
            analysisRepository.updateKeyAndBpm(sId, tonic, mode, bpm)
        }
    }

    fun saveChord(
        chordId: Long,
        startMs: Long,
        endMs: Long,
        rootPitchClass: Int,
        quality: ChordQuality,
        bassPitchClass: Int?
    ) {
        val sId = _currentSongId.value ?: return
        viewModelScope.launch {
            val chord = ChordEventEntity(
                id = chordId,
                songId = sId,
                startMs = startMs,
                endMs = endMs,
                rootPitchClass = rootPitchClass,
                quality = quality.name,
                bassPitchClass = bassPitchClass,
                confidence = 1.0f,
                source = "user"
            )
            analysisRepository.saveChord(chord)
        }
    }

    fun deleteChord(chordId: Long) {
        viewModelScope.launch {
            analysisRepository.deleteChord(chordId)
        }
    }
}
