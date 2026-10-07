package com.talodabi.talomusic.ui.screens.search

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.talodabi.talomusic.ui.components.AlbumCard
import com.talodabi.talomusic.ui.components.SongListItem
import com.talodabi.talomusic.ui.theme.TaloTheme
import com.talodabi.talomusic.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    onPlaylistClick: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TaloTheme.colors.background)
    ) {
        // Search Header & Input Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TaloTheme.spacing.lg,
                    end = TaloTheme.spacing.lg,
                    top = TaloTheme.spacing.xl,
                    bottom = TaloTheme.spacing.md
                )
        ) {
            Text(
                text = "Search",
                style = TaloTheme.typography.h1,
                color = TaloTheme.colors.primaryText
            )
            Spacer(modifier = Modifier.height(TaloTheme.spacing.sm))

            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = {
                    Text(
                        "Songs, artists, albums...",
                        style = TaloTheme.typography.body,
                        color = TaloTheme.colors.secondaryText
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = if (query.isNotEmpty()) TaloTheme.colors.primary else TaloTheme.colors.secondaryText
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = TaloTheme.colors.secondaryText
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = TaloTheme.colors.surfaceElevated,
                    focusedBorderColor = TaloTheme.colors.primary,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = TaloTheme.colors.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (query.trim().isEmpty()) {
            // Empty search state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TaloTheme.colors.tertiaryText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(TaloTheme.spacing.sm))
                    Text(
                        text = "Instant local search",
                        style = TaloTheme.typography.h3,
                        color = TaloTheme.colors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Find any track, album, or artist offline",
                        style = TaloTheme.typography.bodySmall,
                        color = TaloTheme.colors.tertiaryText
                    )
                }
            }
        } else if (searchResults.isEmpty) {
            // No matches
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matches for \"$query\"",
                    style = TaloTheme.typography.body,
                    color = TaloTheme.colors.secondaryText
                )
            }
        } else {
            // Grouped Search Results
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Songs section
                if (searchResults.songs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Songs (${searchResults.songs.size})",
                            style = TaloTheme.typography.h3,
                            color = TaloTheme.colors.primaryText,
                            modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.xs)
                        )
                    }
                    items(searchResults.songs, key = { it.id }) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                            onSongClick = { viewModel.playSong(song, searchResults.songs) },
                            onFavoriteToggle = { viewModel.toggleFavorite(song.id) }
                        )
                    }
                }

                // Albums section
                if (searchResults.albums.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(TaloTheme.spacing.md))
                        Text(
                            text = "Albums (${searchResults.albums.size})",
                            style = TaloTheme.typography.h3,
                            color = TaloTheme.colors.primaryText,
                            modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.xs)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = TaloTheme.spacing.lg),
                            horizontalArrangement = Arrangement.spacedBy(TaloTheme.spacing.md)
                        ) {
                            items(searchResults.albums, key = { it.id }) { album ->
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

                // Artists section
                if (searchResults.artists.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(TaloTheme.spacing.md))
                        Text(
                            text = "Artists (${searchResults.artists.size})",
                            style = TaloTheme.typography.h3,
                            color = TaloTheme.colors.primaryText,
                            modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.xs)
                        )
                    }
                    items(searchResults.artists, key = { it.id }) { artist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val artistSongs = allSongs.filter { it.artist == artist.name }
                                    artistSongs.firstOrNull()?.let {
                                        viewModel.playSong(it, artistSongs)
                                    }
                                }
                                .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(TaloTheme.shapes.circular)
                                    .background(TaloTheme.colors.surfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TaloTheme.colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(TaloTheme.spacing.md))
                            Column {
                                Text(
                                    text = artist.name,
                                    style = TaloTheme.typography.body,
                                    color = TaloTheme.colors.primaryText
                                )
                                Text(
                                    text = "${artist.songCount} songs",
                                    style = TaloTheme.typography.bodySmall,
                                    color = TaloTheme.colors.secondaryText
                                )
                            }
                        }
                    }
                }

                // Playlists section
                if (searchResults.playlists.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(TaloTheme.spacing.md))
                        Text(
                            text = "Playlists (${searchResults.playlists.size})",
                            style = TaloTheme.typography.h3,
                            color = TaloTheme.colors.primaryText,
                            modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.xs)
                        )
                    }
                    items(searchResults.playlists, key = { it.id }) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlaylistClick(playlist.id, playlist.name) }
                                .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = TaloTheme.colors.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(TaloTheme.spacing.md))
                            Text(
                                text = playlist.name,
                                style = TaloTheme.typography.body,
                                color = TaloTheme.colors.primaryText
                            )
                        }
                    }
                }
            }
        }
    }
}
