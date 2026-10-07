package com.talodabi.talomusic.data.repository

import com.talodabi.talomusic.data.analysis.AudioAnalysisEngine
import com.talodabi.talomusic.data.local.dao.AnalysisDao
import com.talodabi.talomusic.data.local.entity.ChordEventEntity
import com.talodabi.talomusic.data.local.entity.LoopPresetEntity
import com.talodabi.talomusic.data.local.entity.SongAnalysisEntity
import com.talodabi.talomusic.data.local.entity.SongSectionEntity
import com.talodabi.talomusic.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AnalysisRepository(
    private val analysisDao: AnalysisDao,
    private val analysisEngine: AudioAnalysisEngine
) {

    fun getAnalysis(songId: Long): Flow<SongAnalysisEntity?> = analysisDao.getAnalysis(songId)

    fun getChords(songId: Long): Flow<List<ChordEventEntity>> = analysisDao.getChordsForSong(songId)

    fun getSections(songId: Long): Flow<List<SongSectionEntity>> = analysisDao.getSectionsForSong(songId)

    fun getLoopPreset(songId: Long): Flow<LoopPresetEntity?> = analysisDao.getLoopPreset(songId)

    /**
     * Retrieves existing analysis or executes automatic analysis and persists the result.
     */
    suspend fun ensureAnalysis(song: Song) = withContext(Dispatchers.IO) {
        val existing = analysisDao.getAnalysisSync(song.id)
        if (existing == null) {
            val result = analysisEngine.analyzeSong(song)
            analysisDao.saveAnalysis(result.analysis)
            analysisDao.insertChords(result.chords)
            analysisDao.insertSections(result.sections)
        }
    }

    suspend fun updateKeyAndBpm(songId: Long, tonic: Int, mode: String, bpm: Int) = withContext(Dispatchers.IO) {
        val existing = analysisDao.getAnalysisSync(songId) ?: SongAnalysisEntity(songId = songId)
        val updated = existing.copy(
            tonicPitchClass = tonic,
            mode = mode,
            bpm = bpm,
            updatedAt = System.currentTimeMillis()
        )
        analysisDao.saveAnalysis(updated)
    }

    suspend fun saveChord(chord: ChordEventEntity) = withContext(Dispatchers.IO) {
        if (chord.id == 0L) {
            analysisDao.insertChord(chord.copy(source = "user", confidence = 1.0f))
        } else {
            analysisDao.updateChord(chord.copy(source = "user", confidence = 1.0f))
        }
    }

    suspend fun deleteChord(chordId: Long) = withContext(Dispatchers.IO) {
        analysisDao.deleteChord(chordId)
    }

    suspend fun saveLoop(songId: Long, startMs: Long, endMs: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        analysisDao.saveLoopPreset(
            LoopPresetEntity(
                songId = songId,
                startMs = startMs,
                endMs = endMs,
                isEnabled = enabled
            )
        )
    }
}
