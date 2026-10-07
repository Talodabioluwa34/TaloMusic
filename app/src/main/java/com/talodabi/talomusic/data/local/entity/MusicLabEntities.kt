package com.talodabi.talomusic.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "song_analyses")
data class SongAnalysisEntity(
    @PrimaryKey
    val songId: Long,
    val tonicPitchClass: Int = 0,
    val mode: String = "MAJOR",
    val bpm: Int = 120,
    val timeSignature: String = "4/4",
    val analysisStatus: String = "ANALYZED",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chord_events",
    indices = [Index(value = ["songId", "startMs"])]
)
data class ChordEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val startMs: Long,
    val endMs: Long,
    val rootPitchClass: Int,
    val quality: String = "MAJOR",
    val bassPitchClass: Int? = null,
    val confidence: Float = 1.0f,
    val source: String = "user" // "automatic", "user", "imported"
)

@Entity(
    tableName = "song_sections",
    indices = [Index(value = ["songId"])]
)
data class SongSectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val startMs: Long,
    val endMs: Long,
    val name: String
)

@Entity(tableName = "loop_presets")
data class LoopPresetEntity(
    @PrimaryKey
    val songId: Long,
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val isEnabled: Boolean = false
)
