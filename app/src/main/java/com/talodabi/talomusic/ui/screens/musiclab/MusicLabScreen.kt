package com.talodabi.talomusic.ui.screens.musiclab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.talodabi.talomusic.data.local.entity.ChordEventEntity
import com.talodabi.talomusic.data.music.ChordQuality
import com.talodabi.talomusic.data.music.MusicChord
import com.talodabi.talomusic.data.music.PitchClass
import com.talodabi.talomusic.ui.components.TaloLogo
import com.talodabi.talomusic.ui.components.TrackSlider
import com.talodabi.talomusic.ui.theme.TaloTheme

@Composable
fun MusicLabScreen(
    songId: Long,
    viewModel: MusicLabViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(songId) {
        viewModel.loadSong(songId)
    }

    val song by viewModel.currentSong.collectAsState()
    val analysis by viewModel.songAnalysis.collectAsState()
    val chords by viewModel.chords.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val transposeOffset by viewModel.transposeOffset.collectAsState()
    val notationMode by viewModel.notationMode.collectAsState()

    var editingChord by remember { mutableStateOf<ChordEventEntity?>(null) }
    var showChordEditor by remember { mutableStateOf(false) }
    var showKeyBpmEditor by remember { mutableStateOf(false) }

    val tonic = analysis?.tonicPitchClass ?: 0
    val mode = analysis?.mode ?: "MAJOR"
    val bpm = analysis?.bpm ?: 120

    // Effective key with transposition applied
    val transposedTonic = Math.floorMod(tonic + transposeOffset, 12)
    val keyDisplayName = "${PitchClass.fromSemitone(transposedTonic).sharpName} ${if (mode == "MAJOR") "Major" else "Minor"}"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TaloTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Workspace Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TaloTheme.spacing.md, vertical = TaloTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TaloTheme.colors.primaryText
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(TaloTheme.shapes.small)
                    .background(TaloTheme.colors.surfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (song?.artworkUri != null) {
                    AsyncImage(
                        model = song?.artworkUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    TaloLogo(size = 18.dp)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song?.title ?: "Loading...",
                    style = TaloTheme.typography.body,
                    color = TaloTheme.colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song?.artist ?: ""} • Music Lab",
                    style = TaloTheme.typography.caption,
                    color = TaloTheme.colors.primary
                )
            }

            IconButton(
                onClick = {
                    editingChord = null
                    showChordEditor = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Chord",
                    tint = TaloTheme.colors.primary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Compact Analysis Summary
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.sm)
                        .clip(TaloTheme.shapes.medium)
                        .background(TaloTheme.colors.surfaceElevated)
                        .clickable { showKeyBpmEditor = true }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "KEY", style = TaloTheme.typography.caption, color = TaloTheme.colors.secondaryText)
                        Text(text = keyDisplayName, style = TaloTheme.typography.body, color = TaloTheme.colors.primaryText)
                    }

                    Column {
                        Text(text = "BPM", style = TaloTheme.typography.caption, color = TaloTheme.colors.secondaryText)
                        Text(text = "$bpm BPM", style = TaloTheme.typography.body, color = TaloTheme.colors.primaryText)
                    }

                    Column {
                        Text(text = "TIME", style = TaloTheme.typography.caption, color = TaloTheme.colors.secondaryText)
                        Text(text = "4/4", style = TaloTheme.typography.body, color = TaloTheme.colors.primaryText)
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Key/BPM",
                        tint = TaloTheme.colors.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Transpose & Notation Mode Controls
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TaloTheme.spacing.lg, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Transposition Stepper
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(TaloTheme.shapes.small)
                            .background(TaloTheme.colors.surfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Transpose: ",
                            style = TaloTheme.typography.caption,
                            color = TaloTheme.colors.secondaryText
                        )
                        Text(
                            text = if (transposeOffset == 0) "0" else (if (transposeOffset > 0) "+$transposeOffset" else "$transposeOffset"),
                            style = TaloTheme.typography.bodySmall,
                            color = TaloTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        Text(
                            text = "[-]",
                            style = TaloTheme.typography.label,
                            color = TaloTheme.colors.primaryText,
                            modifier = Modifier
                                .clickable { viewModel.transpose(-1) }
                                .padding(horizontal = 4.dp)
                        )
                        Text(
                            text = "[+]",
                            style = TaloTheme.typography.label,
                            color = TaloTheme.colors.primaryText,
                            modifier = Modifier
                                .clickable { viewModel.transpose(1) }
                                .padding(horizontal = 4.dp)
                        )
                    }

                    // Notation Mode Pill (Chords / Roman / Nashville)
                    Row(
                        modifier = Modifier
                            .clip(TaloTheme.shapes.small)
                            .background(TaloTheme.colors.surfaceElevated)
                            .padding(2.dp)
                    ) {
                        listOf(
                            NotationMode.CHORDS to "Chords",
                            NotationMode.ROMAN_NUMERALS to "Roman",
                            NotationMode.NASHVILLE_NUMBERS to "1-4-5"
                        ).forEach { (m, label) ->
                            val isSel = notationMode == m
                            Box(
                                modifier = Modifier
                                    .clip(TaloTheme.shapes.small)
                                    .background(if (isSel) TaloTheme.colors.primarySoft else TaloTheme.colors.surfaceElevated)
                                    .clickable { viewModel.setNotationMode(m) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = TaloTheme.typography.caption,
                                    color = if (isSel) TaloTheme.colors.primary else TaloTheme.colors.secondaryText
                                )
                            }
                        }
                    }
                }
            }

            // Song Section Timeline
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Song Sections",
                    style = TaloTheme.typography.label,
                    color = TaloTheme.colors.primary,
                    modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg)
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = TaloTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sections, key = { it.id }) { sec ->
                        val isCurrent = playbackState.currentPositionMs in sec.startMs..sec.endMs
                        Box(
                            modifier = Modifier
                                .clip(TaloTheme.shapes.small)
                                .background(if (isCurrent) TaloTheme.colors.primarySoft else TaloTheme.colors.surfaceElevated)
                                .border(
                                    1.dp,
                                    if (isCurrent) TaloTheme.colors.primary else TaloTheme.colors.divider,
                                    TaloTheme.shapes.small
                                )
                                .clickable { viewModel.seekTo(sec.startMs) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = sec.name,
                                style = TaloTheme.typography.bodySmall,
                                color = if (isCurrent) TaloTheme.colors.primary else TaloTheme.colors.primaryText
                            )
                        }
                    }
                }
            }

            // Chord Timeline
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TaloTheme.spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chord Progression Timeline",
                        style = TaloTheme.typography.label,
                        color = TaloTheme.colors.primary
                    )
                    Text(
                        text = "Tap to edit",
                        style = TaloTheme.typography.caption,
                        color = TaloTheme.colors.secondaryText
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = TaloTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chords, key = { it.id }) { chordEvent ->
                        val isPlayingThisChord = playbackState.currentPositionMs in chordEvent.startMs..chordEvent.endMs
                        val baseChord = MusicChord(
                            rootPitchClass = chordEvent.rootPitchClass,
                            quality = ChordQuality.fromString(chordEvent.quality),
                            bassPitchClass = chordEvent.bassPitchClass
                        )
                        val transposedChord = baseChord.transpose(transposeOffset)

                        val displayNotation = when (notationMode) {
                            NotationMode.CHORDS -> transposedChord.toDisplayString()
                            NotationMode.ROMAN_NUMERALS -> transposedChord.toRomanNumeral(transposedTonic)
                            NotationMode.NASHVILLE_NUMBERS -> transposedChord.toNashvilleNumber(transposedTonic)
                        }

                        Box(
                            modifier = Modifier
                                .width(94.dp)
                                .height(80.dp)
                                .clip(TaloTheme.shapes.medium)
                                .background(if (isPlayingThisChord) TaloTheme.colors.primarySoft else TaloTheme.colors.surfaceElevated)
                                .border(
                                    width = if (isPlayingThisChord) 1.5.dp else 0.5.dp,
                                    color = if (isPlayingThisChord) TaloTheme.colors.primary else TaloTheme.colors.divider,
                                    shape = TaloTheme.shapes.medium
                                )
                                .clickable {
                                    editingChord = chordEvent
                                    showChordEditor = true
                                }
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = displayNotation,
                                    style = TaloTheme.typography.h2,
                                    color = if (isPlayingThisChord) TaloTheme.colors.primary else TaloTheme.colors.primaryText
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${(chordEvent.startMs / 1000)}s",
                                        style = TaloTheme.typography.caption,
                                        color = TaloTheme.colors.secondaryText
                                    )
                                    Text(
                                        text = if (chordEvent.source == "user") "✓" else "Auto",
                                        style = TaloTheme.typography.caption,
                                        color = if (chordEvent.source == "user") TaloTheme.colors.primary else TaloTheme.colors.tertiaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Loop Practice Section (A-B Looper)
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "A-B Loop Practice",
                    style = TaloTheme.typography.label,
                    color = TaloTheme.colors.primary,
                    modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TaloTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val loopStart = playbackState.loopStartMs
                    val loopEnd = playbackState.loopEndMs

                    OutlinedButton(
                        onClick = {
                            val cur = playbackState.currentPositionMs
                            viewModel.setLoopRange(cur, (loopEnd ?: (cur + 8000L)))
                        },
                        shape = TaloTheme.shapes.small,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (loopStart != null) "A: ${(loopStart / 1000)}s" else "Set A", style = TaloTheme.typography.caption)
                    }

                    OutlinedButton(
                        onClick = {
                            val cur = playbackState.currentPositionMs
                            viewModel.setLoopRange(loopStart ?: (cur - 8000L).coerceAtLeast(0L), cur)
                        },
                        shape = TaloTheme.shapes.small,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (loopEnd != null) "B: ${(loopEnd / 1000)}s" else "Set B", style = TaloTheme.typography.caption)
                    }

                    Button(
                        onClick = { viewModel.toggleLoop() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (playbackState.isLoopEnabled) TaloTheme.colors.primary else TaloTheme.colors.surfaceElevated
                        ),
                        shape = TaloTheme.shapes.small,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Loop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (playbackState.isLoopEnabled) "Looping" else "Loop Off",
                            style = TaloTheme.typography.caption,
                            color = if (playbackState.isLoopEnabled) TaloTheme.colors.background else TaloTheme.colors.primaryText
                        )
                    }
                }
            }

            // Playback Speed Selector (Pitch-preserving)
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Playback Speed",
                    style = TaloTheme.typography.label,
                    color = TaloTheme.colors.primary,
                    modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TaloTheme.spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(0.50f, 0.75f, 1.00f, 1.25f, 1.50f).forEach { speed ->
                        val isSelected = playbackState.playbackSpeed == speed
                        Box(
                            modifier = Modifier
                                .clip(TaloTheme.shapes.small)
                                .background(if (isSelected) TaloTheme.colors.primary else TaloTheme.colors.surfaceElevated)
                                .clickable { viewModel.setPlaybackSpeed(speed) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${speed}x",
                                style = TaloTheme.typography.caption,
                                color = if (isSelected) TaloTheme.colors.background else TaloTheme.colors.primaryText
                            )
                        }
                    }
                }
            }
        }

        // Bottom Fixed Player Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TaloTheme.colors.surface)
                .padding(horizontal = TaloTheme.spacing.lg, vertical = 8.dp)
        ) {
            TrackSlider(
                currentPositionMs = playbackState.currentPositionMs,
                durationMs = playbackState.durationMs,
                onSeekTo = { viewModel.seekTo(it) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(TaloTheme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { viewModel.togglePlayPause() }) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                            tint = TaloTheme.colors.background,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }

    if (showChordEditor) {
        val curPos = playbackState.currentPositionMs
        ChordEditorDialog(
            initialChord = editingChord,
            defaultStartMs = curPos,
            defaultEndMs = curPos + 4000L,
            onDismiss = { showChordEditor = false },
            onSave = { chordId, startMs, endMs, root, quality, bass ->
                viewModel.saveChord(chordId, startMs, endMs, root, quality, bass)
            },
            onDelete = { chordId ->
                viewModel.deleteChord(chordId)
            }
        )
    }

    if (showKeyBpmEditor) {
        KeyBpmEditorDialog(
            currentTonic = tonic,
            currentMode = mode,
            currentBpm = bpm,
            onDismiss = { showKeyBpmEditor = false },
            onSave = { newTonic, newMode, newBpm ->
                viewModel.updateKeyAndBpm(newTonic, newMode, newBpm)
            }
        )
    }
}
