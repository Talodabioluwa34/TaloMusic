package com.talodabi.talomusic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.talodabi.talomusic.playback.PlaybackUiState
import com.talodabi.talomusic.ui.theme.TaloTheme

@Composable
fun MiniPlayer(
    playbackState: PlaybackUiState,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong = playbackState.currentSong ?: return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TaloTheme.spacing.md, vertical = 6.dp)
            .clip(TaloTheme.shapes.medium)
            .background(TaloTheme.colors.surfaceElevated)
            .border(0.5.dp, TaloTheme.colors.divider, TaloTheme.shapes.medium)
            .clickable(onClick = onExpand)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = TaloTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album artwork thumbnail
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(TaloTheme.shapes.small)
                        .background(TaloTheme.colors.surfaceStrong),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentSong.artworkUri != null) {
                        AsyncImage(
                            model = currentSong.artworkUri,
                            contentDescription = "Cover for ${currentSong.title}",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        TaloLogo(size = 20.dp, tint = TaloTheme.colors.primary.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.width(TaloTheme.spacing.sm))

                // Track Title and Artist
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = TaloTheme.spacing.xs)
                ) {
                    Text(
                        text = currentSong.title,
                        style = TaloTheme.typography.body,
                        color = TaloTheme.colors.primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong.artist,
                        style = TaloTheme.typography.bodySmall,
                        color = TaloTheme.colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play / Pause Action Button
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        tint = TaloTheme.colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Next Track Button
                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next track",
                        tint = TaloTheme.colors.primaryText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Continuous Micro-progress line at the bottom
            LinearProgressIndicator(
                progress = { playbackState.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = TaloTheme.colors.primary,
                trackColor = TaloTheme.colors.divider.copy(alpha = 0.4f),
            )
        }
    }
}
