package com.talodabi.talomusic.ui.screens.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.talodabi.talomusic.ui.components.SongListItem
import com.talodabi.talomusic.ui.theme.TaloTheme
import com.talodabi.talomusic.ui.viewmodel.MusicViewModel

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    playlistName: String,
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.getSongsForPlaylist(playlistId).collectAsState(initial = emptyList())
    val playbackState by viewModel.playbackState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TaloTheme.colors.background)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TaloTheme.spacing.md, vertical = TaloTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TaloTheme.colors.primaryText
                )
            }

            IconButton(onClick = {
                viewModel.deletePlaylist(playlistId)
                onBack()
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Playlist",
                    tint = TaloTheme.colors.secondaryText
                )
            }
        }

        // Header Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(TaloTheme.shapes.large)
                    .background(TaloTheme.colors.surfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = TaloTheme.colors.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(TaloTheme.spacing.md))

            Column {
                Text(
                    text = playlistName,
                    style = TaloTheme.typography.h1,
                    color = TaloTheme.colors.primaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${songs.size} songs",
                    style = TaloTheme.typography.bodySmall,
                    color = TaloTheme.colors.secondaryText
                )
            }
        }

        // Action Buttons: Play All / Shuffle
        if (songs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(TaloTheme.spacing.md)
            ) {
                Button(
                    onClick = { viewModel.playSong(songs.first(), songs) },
                    colors = ButtonDefaults.buttonColors(containerColor = TaloTheme.colors.primary),
                    shape = TaloTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = TaloTheme.colors.background)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Play All", color = TaloTheme.colors.background, style = TaloTheme.typography.label)
                }

                OutlinedButton(
                    onClick = {
                        val shuffled = songs.shuffled()
                        viewModel.playSong(shuffled.first(), shuffled)
                    },
                    shape = TaloTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, tint = TaloTheme.colors.primaryText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Shuffle", color = TaloTheme.colors.primaryText, style = TaloTheme.typography.label)
                }
            }
        }

        Spacer(modifier = Modifier.height(TaloTheme.spacing.sm))

        // Songs in playlist
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "This playlist is empty. Add songs from your Library.",
                    style = TaloTheme.typography.bodySmall,
                    color = TaloTheme.colors.secondaryText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(songs, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                        onSongClick = { viewModel.playSong(song, songs) },
                        onFavoriteToggle = { viewModel.toggleFavorite(song.id) }
                    )
                }
            }
        }
    }
}
