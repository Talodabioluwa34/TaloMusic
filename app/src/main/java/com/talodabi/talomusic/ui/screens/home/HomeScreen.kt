package com.talodabi.talomusic.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.talodabi.talomusic.data.model.Song
import com.talodabi.talomusic.ui.components.AlbumCard
import com.talodabi.talomusic.ui.components.SongListItem
import com.talodabi.talomusic.ui.components.TaloLogo
import com.talodabi.talomusic.ui.theme.TaloTheme
import com.talodabi.talomusic.ui.viewmodel.MusicViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onPlaylistClick: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TaloTheme.colors.background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Hero Header & Brand
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TaloTheme.spacing.lg,
                        end = TaloTheme.spacing.lg,
                        top = TaloTheme.spacing.xl,
                        bottom = TaloTheme.spacing.md
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = TaloTheme.typography.h1,
                        color = TaloTheme.colors.primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your music. Your space.",
                        style = TaloTheme.typography.bodySmall,
                        color = TaloTheme.colors.secondaryText
                    )
                }

                TaloLogo(size = 36.dp)
            }
        }

        // Quick Favorites Pill
        if (favoriteSongs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.xs)
                        .clip(TaloTheme.shapes.medium)
                        .background(TaloTheme.colors.surfaceElevated)
                        .clickable {
                            favoriteSongs.firstOrNull()?.let {
                                viewModel.playSong(it, favoriteSongs)
                            }
                        }
                        .padding(horizontal = TaloTheme.spacing.md, vertical = TaloTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorites",
                        tint = TaloTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(TaloTheme.spacing.sm))
                    Text(
                        text = "Favorites",
                        style = TaloTheme.typography.h3,
                        color = TaloTheme.colors.primaryText
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${favoriteSongs.size} tracks",
                        style = TaloTheme.typography.bodySmall,
                        color = TaloTheme.colors.secondaryText
                    )
                }
            }
        }

        // Recently Played Albums Section
        if (recentlyPlayed.isNotEmpty() || albums.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(TaloTheme.spacing.lg))
                Text(
                    text = "Recently Played",
                    style = TaloTheme.typography.h2,
                    color = TaloTheme.colors.primaryText,
                    modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg)
                )
                Spacer(modifier = Modifier.height(TaloTheme.spacing.sm))

                val displayAlbums = if (albums.isNotEmpty()) albums.take(8) else emptyList()
                LazyRow(
                    contentPadding = PaddingValues(horizontal = TaloTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(TaloTheme.spacing.md)
                ) {
                    items(displayAlbums, key = { it.id }) { album ->
                        AlbumCard(
                            album = album,
                            onClick = {
                                val albumSongs = allSongs.filter { it.album == album.title }
                                albumSongs.firstOrNull()?.let {
                                    viewModel.playSong(it, albumSongs)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Your Playlists Section
        item {
            Spacer(modifier = Modifier.height(TaloTheme.spacing.xl))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TaloTheme.spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Playlists",
                    style = TaloTheme.typography.h2,
                    color = TaloTheme.colors.primaryText
                )

                TextButton(onClick = { showCreatePlaylistDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create playlist",
                        tint = TaloTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New",
                        style = TaloTheme.typography.label,
                        color = TaloTheme.colors.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(TaloTheme.spacing.sm))

            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = TaloTheme.spacing.lg)
                        .clip(TaloTheme.shapes.medium)
                        .background(TaloTheme.colors.surfaceElevated)
                        .padding(TaloTheme.spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No playlists yet. Tap 'New' to create one.",
                        style = TaloTheme.typography.bodySmall,
                        color = TaloTheme.colors.secondaryText
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = TaloTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(TaloTheme.spacing.md)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(90.dp)
                                .clip(TaloTheme.shapes.medium)
                                .background(TaloTheme.colors.surfaceElevated)
                                .clickable { onPlaylistClick(playlist.id, playlist.name) }
                                .padding(TaloTheme.spacing.md),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = TaloTheme.colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = playlist.name,
                                    style = TaloTheme.typography.body,
                                    color = TaloTheme.colors.primaryText,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recently Added Songs Section
        item {
            Spacer(modifier = Modifier.height(TaloTheme.spacing.xl))
            Text(
                text = "Recently Added",
                style = TaloTheme.typography.h2,
                color = TaloTheme.colors.primaryText,
                modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg)
            )
            Spacer(modifier = Modifier.height(TaloTheme.spacing.xs))
        }

        val recentAddedSongs = allSongs.take(15)
        if (recentAddedSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(TaloTheme.spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No audio tracks detected. Pull to scan library.",
                        style = TaloTheme.typography.bodySmall,
                        color = TaloTheme.colors.secondaryText
                    )
                }
            }
        } else {
            items(recentAddedSongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                    onSongClick = { viewModel.playSong(song, recentAddedSongs) },
                    onFavoriteToggle = { viewModel.toggleFavorite(song.id) }
                )
            }
        }
    }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreatePlaylistDialog = false
                newPlaylistName = ""
            },
            title = {
                Text(text = "New Playlist", style = TaloTheme.typography.h2)
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TaloTheme.colors.primary)
                ) {
                    Text("Create", color = TaloTheme.colors.background)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreatePlaylistDialog = false
                    newPlaylistName = ""
                }) {
                    Text("Cancel", color = TaloTheme.colors.secondaryText)
                }
            }
        )
    }
}
