package com.talodabi.talomusic.ui.screens.nowplaying

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.talodabi.talomusic.playback.RepeatMode
import com.talodabi.talomusic.ui.components.SongListItem
import com.talodabi.talomusic.ui.components.TaloLogo
import com.talodabi.talomusic.ui.components.TrackSlider
import com.talodabi.talomusic.ui.theme.TaloTheme
import com.talodabi.talomusic.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit,
    onOpenEqualizer: () -> Unit = {},
    onOpenMusicLab: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val song = playbackState.currentSong ?: return

    var showQueueSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Gesture tracking for swipe left (next), swipe right (previous), swipe down (dismiss)
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TaloTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = TaloTheme.spacing.lg)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (totalDragY > 180f && Math.abs(totalDragY) > Math.abs(totalDragX)) {
                            // Swipe down to dismiss
                            onDismiss()
                        } else if (totalDragX < -150f) {
                            // Swipe left to skip next
                            viewModel.skipToNext()
                        } else if (totalDragX > 150f) {
                            // Swipe right to skip previous
                            viewModel.skipToPrevious()
                        }
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = TaloTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Dismiss Now Playing",
                    tint = TaloTheme.colors.primaryText,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "Now Playing",
                style = TaloTheme.typography.label,
                color = TaloTheme.colors.secondaryText
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onOpenMusicLab(song.id) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Open in Music Lab",
                        tint = TaloTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { showQueueSheet = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "View Queue",
                        tint = TaloTheme.colors.primaryText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(TaloTheme.spacing.xs))

        // Hero Album Artwork
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(TaloTheme.shapes.hero)
                .background(TaloTheme.colors.surfaceElevated)
                .shadow(elevation = 16.dp, shape = TaloTheme.shapes.hero),
            contentAlignment = Alignment.Center
        ) {
            if (song.artworkUri != null) {
                AsyncImage(
                    model = song.artworkUri,
                    contentDescription = "Artwork for ${song.title}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                TaloLogo(size = 96.dp, tint = TaloTheme.colors.primary.copy(alpha = 0.8f))
            }
        }

        Spacer(modifier = Modifier.height(TaloTheme.spacing.lg))

        // Song Title, Artist & Favorite Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = TaloTheme.typography.h2,
                    color = TaloTheme.colors.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.artist,
                    style = TaloTheme.typography.body,
                    color = TaloTheme.colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = { viewModel.toggleFavorite(song.id) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (song.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (song.isFavorite) TaloTheme.colors.primary else TaloTheme.colors.tertiaryText,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(TaloTheme.spacing.md))

        // Progress Slider
        TrackSlider(
            currentPositionMs = playbackState.currentPositionMs,
            durationMs = playbackState.durationMs,
            onSeekTo = { viewModel.seekTo(it) }
        )

        Spacer(modifier = Modifier.height(TaloTheme.spacing.sm))

        // Primary Playback Controls: Previous / Play-Pause / Next
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Track Button
            IconButton(
                onClick = { viewModel.skipToPrevious() },
                modifier = Modifier.size(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = TaloTheme.colors.primaryText,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Hero Play/Pause Button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(TaloTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(68.dp)
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        tint = TaloTheme.colors.background,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            // Next Track Button
            IconButton(
                onClick = { viewModel.skipToNext() },
                modifier = Modifier.size(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = TaloTheme.colors.primaryText,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(TaloTheme.spacing.sm))

        // Secondary Controls: Shuffle, Repeat, Equalizer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = TaloTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle Button
            IconButton(
                onClick = { viewModel.toggleShuffle() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playbackState.isShuffleEnabled) TaloTheme.colors.primary else TaloTheme.colors.tertiaryText,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Repeat Button
            IconButton(
                onClick = { viewModel.cycleRepeatMode() },
                modifier = Modifier.size(48.dp)
            ) {
                val (repeatIcon, isRepeatActive) = when (playbackState.repeatMode) {
                    RepeatMode.ONE -> Icons.Default.RepeatOne to true
                    RepeatMode.ALL -> Icons.Default.Repeat to true
                    RepeatMode.OFF -> Icons.Default.Repeat to false
                }
                Icon(
                    imageVector = repeatIcon,
                    contentDescription = "Repeat",
                    tint = if (isRepeatActive) TaloTheme.colors.primary else TaloTheme.colors.tertiaryText,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Equalizer Button
            IconButton(
                onClick = onOpenEqualizer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = "Equalizer",
                    tint = TaloTheme.colors.tertiaryText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    // Up Next Queue Sheet
    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = sheetState,
            containerColor = TaloTheme.colors.surfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TaloTheme.spacing.lg)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Up Next (${playbackState.queue.size})",
                    style = TaloTheme.typography.h2,
                    color = TaloTheme.colors.primaryText,
                    modifier = Modifier.padding(vertical = TaloTheme.spacing.sm)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    itemsIndexed(playbackState.queue, key = { index, s -> "${s.id}_$index" }) { index, qSong ->
                        SongListItem(
                            song = qSong,
                            isPlaying = playbackState.currentSong?.id == qSong.id && playbackState.isPlaying,
                            onSongClick = { viewModel.playSong(qSong, playbackState.queue) },
                            onFavoriteToggle = { viewModel.toggleFavorite(qSong.id) }
                        )
                    }
                }
            }
        }
    }
}
