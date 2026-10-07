package com.talodabi.talomusic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.talodabi.talomusic.data.local.entity.ChordEventEntity
import com.talodabi.talomusic.data.local.entity.LoopPresetEntity
import com.talodabi.talomusic.data.local.entity.SongAnalysisEntity
import com.talodabi.talomusic.data.local.entity.SongSectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {

    @Query("SELECT * FROM song_analyses WHERE songId = :songId")
    fun getAnalysis(songId: Long): Flow<SongAnalysisEntity?>

    @Query("SELECT * FROM song_analyses WHERE songId = :songId")
    suspend fun getAnalysisSync(songId: Long): SongAnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAnalysis(analysis: SongAnalysisEntity)

    @Query("SELECT * FROM chord_events WHERE songId = :songId ORDER BY startMs ASC")
    fun getChordsForSong(songId: Long): Flow<List<ChordEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChord(chord: ChordEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChords(chords: List<ChordEventEntity>)

    @Update
    suspend fun updateChord(chord: ChordEventEntity)

    @Query("DELETE FROM chord_events WHERE id = :chordId")
    suspend fun deleteChord(chordId: Long)

    @Query("DELETE FROM chord_events WHERE songId = :songId")
    suspend fun clearChords(songId: Long)

    @Query("SELECT * FROM song_sections WHERE songId = :songId ORDER BY startMs ASC")
    fun getSectionsForSong(songId: Long): Flow<List<SongSectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SongSectionEntity>)

    @Query("SELECT * FROM loop_presets WHERE songId = :songId")
    fun getLoopPreset(songId: Long): Flow<LoopPresetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLoopPreset(preset: LoopPresetEntity)
}
