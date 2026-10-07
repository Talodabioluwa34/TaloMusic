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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.talodabi.talomusic.data.local.entity.ChordEventEntity
import com.talodabi.talomusic.data.music.ChordQuality
import com.talodabi.talomusic.data.music.MusicChord
import com.talodabi.talomusic.data.music.PitchClass
import com.talodabi.talomusic.ui.theme.TaloTheme

@Composable
fun ChordEditorDialog(
    initialChord: ChordEventEntity?,
    defaultStartMs: Long,
    defaultEndMs: Long,
    onDismiss: () -> Unit,
    onSave: (chordId: Long, startMs: Long, endMs: Long, rootPitchClass: Int, quality: ChordQuality, bassPitchClass: Int?) -> Unit,
    onDelete: (chordId: Long) -> Unit
) {
    var selectedRoot by remember { mutableIntStateOf(initialChord?.rootPitchClass ?: 0) }
    var selectedQuality by remember {
        mutableStateOf(
            initialChord?.let { ChordQuality.fromString(it.quality) } ?: ChordQuality.MAJOR
        )
    }

    val previewChord = remember(selectedRoot, selectedQuality) {
        MusicChord(selectedRoot, selectedQuality).toDisplayString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialChord != null) "Edit Chord ($previewChord)" else "Add Chord ($previewChord)",
                    style = TaloTheme.typography.h2,
                    color = TaloTheme.colors.primaryText
                )

                if (initialChord != null) {
                    IconButton(onClick = { onDelete(initialChord.id); onDismiss() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Chord",
                            tint = TaloTheme.colors.secondaryText
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Root Pitch",
                    style = TaloTheme.typography.label,
                    color = TaloTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                // 12 Chromatic Pitch Selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PitchClass.entries) { pc ->
                        val isSelected = selectedRoot == pc.semitone
                        Box(
                            modifier = Modifier
                                .clip(TaloTheme.shapes.small)
                                .background(if (isSelected) TaloTheme.colors.primary else TaloTheme.colors.surfaceStrong)
                                .clickable { selectedRoot = pc.semitone }
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

                Text(
                    text = "Quality",
                    style = TaloTheme.typography.label,
                    color = TaloTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Chord Qualities Selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ChordQuality.entries) { q ->
                        val isSelected = selectedQuality == q
                        val label = if (q.symbol.isEmpty()) "Maj" else q.symbol
                        Box(
                            modifier = Modifier
                                .clip(TaloTheme.shapes.small)
                                .background(if (isSelected) TaloTheme.colors.primary else TaloTheme.colors.surfaceStrong)
                                .clickable { selectedQuality = q }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                style = TaloTheme.typography.bodySmall,
                                color = if (isSelected) TaloTheme.colors.background else TaloTheme.colors.primaryText
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialChord?.id ?: 0L,
                        initialChord?.startMs ?: defaultStartMs,
                        initialChord?.endMs ?: defaultEndMs,
                        selectedRoot,
                        selectedQuality,
                        null
                    )
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
