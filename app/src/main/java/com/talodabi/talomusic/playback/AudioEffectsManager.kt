package com.talodabi.talomusic.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val minLevelMilliBels: Short,
    val maxLevelMilliBels: Short,
    val currentLevelMilliBels: Short
)

data class EqualizerState(
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val bassBoostStrength: Short = 0,
    val virtualizerStrength: Short = 0,
    val presets: List<String> = emptyList(),
    val currentPreset: String = "Custom"
)

class AudioEffectsManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0) return
        release()

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }

            refreshState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun refreshState() {
        val eq = equalizer ?: return
        val numBands = eq.numberOfBands
        val minLevel = eq.bandLevelRange[0]
        val maxLevel = eq.bandLevelRange[1]

        val bands = (0 until numBands).map { bandIdx ->
            val shortIdx = bandIdx.toShort()
            EqualizerBand(
                index = shortIdx,
                centerFreqHz = eq.getCenterFreq(shortIdx) / 1000,
                minLevelMilliBels = minLevel,
                maxLevelMilliBels = maxLevel,
                currentLevelMilliBels = eq.getBandLevel(shortIdx)
            )
        }

        val presets = (0 until eq.numberOfPresets).map { eq.getPresetName(it.toShort()) }

        _state.value = _state.value.copy(
            bands = bands,
            presets = presets,
            bassBoostStrength = bassBoost?.roundedStrength ?: 0,
            virtualizerStrength = virtualizer?.roundedStrength ?: 0
        )
    }

    fun setEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
            _state.value = _state.value.copy(isEnabled = enabled)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBandLevel(bandIndex: Short, levelMilliBels: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, levelMilliBels)
            val updatedBands = _state.value.bands.map {
                if (it.index == bandIndex) it.copy(currentLevelMilliBels = levelMilliBels) else it
            }
            _state.value = _state.value.copy(bands = updatedBands, currentPreset = "Custom")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBassBoost(strength: Short) {
        try {
            bassBoost?.setStrength(strength.coerceIn(0, 1000))
            _state.value = _state.value.copy(bassBoostStrength = strength)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPreset(presetIndex: Short) {
        try {
            equalizer?.usePreset(presetIndex)
            val presetName = equalizer?.getPresetName(presetIndex) ?: "Preset"
            refreshState()
            _state.value = _state.value.copy(currentPreset = presetName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}
