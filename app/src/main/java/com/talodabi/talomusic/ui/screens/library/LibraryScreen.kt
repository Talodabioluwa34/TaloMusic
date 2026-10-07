package com.talodabi.talomusic.ui.screens.library

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.talodabi.talomusic.ui.theme.TaloTheme
import com.talodabi.talomusic.ui.viewmodel.MusicViewModel

enum class SongSortOrder {
    TITLE_ASC, ARTIST_ASC, DATE_ADDED_DESC, DURATION_DESC
}

@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Songs", "Albums", "Artists", "Genres")

    var sortOrder by remember { mutableStateOf(SongSortOrder.TITLE_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }

    val sortedSongs = remember(allSongs, sortOrder) {
        when (sortOrder) {
            SongSortOrder.TITLE_ASC -> allSongs.sortedBy { it.title.lowercase() }
            SongSortOrder.ARTIST_ASC -> allSongs.sortedBy { it.artist.lowercase() }
            SongSortOrder.DATE_ADDED_DESC -> allSongs.sortedByDescending { it.dateAdded }
            SongSortOrder.DURATION_DESC -> allSongs.sortedByDescending { it.durationMs }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TaloTheme.colors.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TaloTheme.spacing.lg,
                    end = TaloTheme.spacing.lg,
                    top = TaloTheme.spacing.xl,
                    bottom = TaloTheme.spacing.sm
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                style = TaloTheme.typography.h1,
                color = TaloTheme.colors.primaryText
            )

            if (selectedTabIndex == 0) {
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort songs",
                            tint = TaloTheme.colors.primaryText
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Title (A-Z)") },
                            onClick = {
                                sortOrder = SongSortOrder.TITLE_ASC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Artist (A-Z)") },
                            onClick = {
                                sortOrder = SongSortOrder.ARTIST_ASC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Recently Added") },
                            onClick = {
                                sortOrder = SongSortOrder.DATE_ADDED_DESC
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duration") },
                            onClick = {
                                sortOrder = SongSortOrder.DURATION_DESC
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Category Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = TaloTheme.spacing.lg,
            containerColor = TaloTheme.colors.background,
            contentColor = TaloTheme.colors.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = TaloTheme.colors.primary,
                    height = 2.5.dp
                )
            },
            divider = { Divider(color = TaloTheme.colors.divider, thickness = 0.5.dp) }
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = TaloTheme.typography.h3,
                            color = if (isSelected) TaloTheme.colors.primary else TaloTheme.colors.secondaryText
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> {
                // Songs Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    item {
                        Text(
                            text = "${sortedSongs.size} songs",
                            style = TaloTheme.typography.caption,
                            color = TaloTheme.colors.secondaryText,
                            modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.sm)
                        )
                    }
                    items(sortedSongs, key = { it.id }) { song ->
                        SongListItem(
                            song = song,
                            isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                            onSongClick = { viewModel.playSong(song, sortedSongs) },
                            onFavoriteToggle = { viewModel.toggleFavorite(song.id) }
                        )
                    }
                }
            }

            1 -> {
                // Albums Tab
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = TaloTheme.spacing.lg,
                        end = TaloTheme.spacing.lg,
                        top = TaloTheme.spacing.md,
                        bottom = 120.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(TaloTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(TaloTheme.spacing.md)
                ) {
                    items(albums, key = { it.id }) { album ->
                        AlbumCard(
                            album = album,
                            onClick = {
                                val albumSongs = allSongs.filter { it.album == album.title }
                                albumSongs.firstOrNull()?.let {
                                    viewModel.playSong(it, albumSongs)
                                }
                            },
                            cardWidth = 160.dp
                        )
                    }
                }
            }

            2 -> {
                // Artists Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(artists, key = { it.id }) { artist ->
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
                                    .size(48.dp)
                                    .clip(TaloTheme.shapes.circular)
                                    .background(TaloTheme.colors.surfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TaloTheme.colors.primary,
                                    modifier = Modifier.size(24.dp)
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
                                    text = "${artist.songCount} songs • ${artist.albumCount} albums",
                                    style = TaloTheme.typography.bodySmall,
                                    color = TaloTheme.colors.secondaryText
                                )
                            }
                        }
                    }
                }
            }

            3 -> {
                // Genres Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(genres, key = { it.id }) { genre ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val genreSongs = allSongs.filter { it.genre == genre.name }
                                    genreSongs.firstOrNull()?.let {
                                        viewModel.playSong(it, genreSongs)
                                    }
                                }
                                .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = genre.name,
                                    style = TaloTheme.typography.body,
                                    color = TaloTheme.colors.primaryText
                                )
                                Text(
                                    text = "${genre.songCount} tracks",
                                    style = TaloTheme.typography.bodySmall,
                                    color = TaloTheme.colors.secondaryText
                                )
                            }
                        }
                        Divider(color = TaloTheme.colors.divider, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
