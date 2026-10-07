package com.talodabi.talomusic.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.talodabi.talomusic.data.model.Song
import com.talodabi.talomusic.ui.theme.TaloTheme

@Composable
fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    onSongClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToPlaylistClick: () -> Unit = {},
    onOpenMusicLab: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onSongClick)
            .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album artwork or fallback
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(TaloTheme.shapes.small)
                .background(TaloTheme.colors.surfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            if (song.artworkUri != null) {
                AsyncImage(
                    model = song.artworkUri,
                    contentDescription = "Cover for ${song.title}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                TaloLogo(size = 22.dp, tint = TaloTheme.colors.primary.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.width(TaloTheme.spacing.md))

        // Title and Artist
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = TaloTheme.spacing.sm)
        ) {
            Text(
                text = song.title,
                style = TaloTheme.typography.body,
                color = if (isPlaying) TaloTheme.colors.primary else TaloTheme.colors.primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} • ${song.formattedDuration}",
                style = TaloTheme.typography.bodySmall,
                color = TaloTheme.colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Favorite Toggle
        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (song.isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (song.isFavorite) TaloTheme.colors.primary else TaloTheme.colors.tertiaryText,
                modifier = Modifier.size(20.dp)
            )
        }

        // More Menu
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Song options",
                    tint = TaloTheme.colors.secondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Add to Playlist", style = TaloTheme.typography.body) },
                    onClick = {
                        showMenu = false
                        onAddToPlaylistClick()
                    }
                )
                if (onOpenMusicLab != null) {
                    DropdownMenuItem(
                        text = { Text("Open in Music Lab", style = TaloTheme.typography.body, color = TaloTheme.colors.primary) },
                        onClick = {
                            showMenu = false
                            onOpenMusicLab()
                        }
                    )
                }
            }
        }
    }
}
