package com.talodabi.talomusic.ui.screens.musiclab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.talodabi.talomusic.data.music.PitchClass
import com.talodabi.talomusic.ui.theme.TaloTheme

@Composable
fun KeyBpmEditorDialog(
    currentTonic: Int,
    currentMode: String,
    currentBpm: Int,
    onDismiss: () -> Unit,
    onSave: (tonic: Int, mode: String, bpm: Int) -> Unit
) {
    var selectedTonic by remember { mutableIntStateOf(currentTonic) }
    var selectedMode by remember { mutableStateOf(currentMode) }
    var selectedBpm by remember { mutableFloatStateOf(currentBpm.toFloat()) }

    val tonicName = PitchClass.fromSemitone(selectedTonic).sharpName
    val keyTitle = "$tonicName ${if (selectedMode == "MAJOR") "Major" else "Minor"}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Song Key & Tempo",
                style = TaloTheme.typography.h2,
                color = TaloTheme.colors.primaryText
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Key: $keyTitle",
                    style = TaloTheme.typography.h3,
                    color = TaloTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Tonic pitch row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PitchClass.entries) { pc ->
                        val isSelected = selectedTonic == pc.semitone
                        Box(
                            modifier = Modifier
                                .clip(TaloTheme.shapes.small)
                                .background(if (isSelected) TaloTheme.colors.primary else TaloTheme.colors.surfaceStrong)
                                .clickable { selectedTonic = pc.semitone }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = pc.sharpName,
                                style = TaloTheme.typography.bodySmall,
                                color = if (isSelected) TaloTheme.colors.background else TaloTheme.colors.primaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(TaloTheme.spacing.md))

                // Mode Row (Major / Minor)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("MAJOR" to "Major", "MINOR" to "Minor").forEach { (mVal, mLabel) ->
                        val isSelected = selectedMode == mVal
                        Box(
                            modifier = Modifier
                                .clip(TaloTheme.shapes.small)
                                .background(if (isSelected) TaloTheme.colors.primary else TaloTheme.colors.surfaceStrong)
                                .clickable { selectedMode = mVal }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = mLabel,
                                style = TaloTheme.typography.bodySmall,
                                color = if (isSelected) TaloTheme.colors.background else TaloTheme.colors.primaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(TaloTheme.spacing.lg))

                // BPM Slider
                Text(
                    text = "Tempo (${selectedBpm.toInt()} BPM)",
                    style = TaloTheme.typography.label,
                    color = TaloTheme.colors.primary
                )
                Slider(
                    value = selectedBpm,
                    onValueChange = { selectedBpm = it },
                    valueRange = 60f..200f,
                    colors = SliderDefaults.colors(
                        thumbColor = TaloTheme.colors.primary,
                        activeTrackColor = TaloTheme.colors.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedTonic, selectedMode, selectedBpm.toInt())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TaloTheme.colors.primary)
            ) {
                Text("Save", color = TaloTheme.colors.background)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TaloTheme.colors.secondaryText)
            }
        },
        containerColor = TaloTheme.colors.surfaceElevated
    )
}
