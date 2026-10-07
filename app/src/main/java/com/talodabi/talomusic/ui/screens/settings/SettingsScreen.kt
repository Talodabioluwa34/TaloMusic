package com.talodabi.talomusic.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.talodabi.talomusic.data.repository.ThemeMode
import com.talodabi.talomusic.ui.components.EqualizerDialog
import com.talodabi.talomusic.ui.components.TaloLogo
import com.talodabi.talomusic.ui.theme.TaloTheme
import com.talodabi.talomusic.ui.viewmodel.MusicViewModel

@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    var resumePlayback by remember { mutableStateOf(true) }
    var autoPlayNext by remember { mutableStateOf(true) }
    var showEqualizer by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TaloTheme.colors.background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
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
                    text = "Settings",
                    style = TaloTheme.typography.h1,
                    color = TaloTheme.colors.primaryText
                )
            }
        }

        // Appearance Section
        item {
            SettingsSectionHeader(title = "Appearance")
            ThemeSelectionRow(
                currentMode = themeMode,
                onModeSelected = { viewModel.setThemeMode(it) }
            )
            Divider(color = TaloTheme.colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg))
        }

        // Playback Section
        item {
            Spacer(modifier = Modifier.height(TaloTheme.spacing.md))
            SettingsSectionHeader(title = "Playback")

            SettingsSwitchRow(
                title = "Resume playback",
                subtitle = "Restore last track and position on launch",
                checked = resumePlayback,
                onCheckedChange = { resumePlayback = it }
            )

            SettingsSwitchRow(
                title = "Continuous play",
                subtitle = "Automatically play next track in queue",
                checked = autoPlayNext,
                onCheckedChange = { autoPlayNext = it }
            )
            Divider(color = TaloTheme.colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg))
        }

        // Audio Effects Section
        item {
            Spacer(modifier = Modifier.height(TaloTheme.spacing.md))
            SettingsSectionHeader(title = "Audio Engine")

            SettingsClickableRow(
                icon = Icons.Default.Equalizer,
                title = "Equalizer & Acoustic Controls",
                subtitle = "Hardware 5-band EQ and bass enhancement",
                onClick = { showEqualizer = true }
            )
            Divider(color = TaloTheme.colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg))
        }

        // Library Section
        item {
            Spacer(modifier = Modifier.height(TaloTheme.spacing.md))
            SettingsSectionHeader(title = "Local Library")

            SettingsClickableRow(
                icon = Icons.Default.Refresh,
                title = "Rescan Media Library",
                subtitle = "Index new audio files from device storage",
                onClick = { viewModel.refreshLibrary() }
            )
            Divider(color = TaloTheme.colors.divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg))
        }

        // About Talo Music Section
        item {
            Spacer(modifier = Modifier.height(TaloTheme.spacing.lg))
            SettingsSectionHeader(title = "About")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.md)
                    .clip(TaloTheme.shapes.medium)
                    .background(TaloTheme.colors.surfaceElevated)
                    .padding(TaloTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TaloLogo(size = 48.dp)
                Spacer(modifier = Modifier.height(TaloTheme.spacing.sm))
                Text(
                    text = "Talo Music",
                    style = TaloTheme.typography.h2,
                    color = TaloTheme.colors.primaryText
                )
                Text(
                    text = "Your music. Your space.",
                    style = TaloTheme.typography.bodySmall,
                    color = TaloTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version 1.0.0 (Native Android)",
                    style = TaloTheme.typography.caption,
                    color = TaloTheme.colors.secondaryText
                )
                Text(
                    text = "Offline-first • Zero tracking • Media3 Core",
                    style = TaloTheme.typography.caption,
                    color = TaloTheme.colors.tertiaryText
                )
            }
        }
    }

    if (showEqualizer) {
        EqualizerDialog(onDismiss = { showEqualizer = false })
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = TaloTheme.typography.label,
        color = TaloTheme.colors.primary,
        modifier = Modifier.padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.xs)
    )
}

@Composable
fun ThemeSelectionRow(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(TaloTheme.spacing.md)
    ) {
        ThemePill(
            title = "Dark",
            icon = Icons.Default.DarkMode,
            isSelected = currentMode == ThemeMode.DARK,
            onClick = { onModeSelected(ThemeMode.DARK) },
            modifier = Modifier.weight(1f)
        )
        ThemePill(
            title = "Light",
            icon = Icons.Default.LightMode,
            isSelected = currentMode == ThemeMode.LIGHT,
            onClick = { onModeSelected(ThemeMode.LIGHT) },
            modifier = Modifier.weight(1f)
        )
        ThemePill(
            title = "System",
            icon = Icons.Default.SettingsBrightness,
            isSelected = currentMode == ThemeMode.SYSTEM,
            onClick = { onModeSelected(ThemeMode.SYSTEM) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ThemePill(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) TaloTheme.colors.primarySoft else TaloTheme.colors.surfaceElevated
    val tint = if (isSelected) TaloTheme.colors.primary else TaloTheme.colors.secondaryText

    Row(
        modifier = modifier
            .clip(TaloTheme.shapes.medium)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = TaloTheme.spacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = title, style = TaloTheme.typography.bodySmall, color = tint)
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = TaloTheme.typography.body, color = TaloTheme.colors.primaryText)
            Text(text = subtitle, style = TaloTheme.typography.bodySmall, color = TaloTheme.colors.secondaryText)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TaloTheme.colors.primary,
                checkedTrackColor = TaloTheme.colors.primarySoft
            )
        )
    }
}

@Composable
fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = TaloTheme.spacing.lg, vertical = TaloTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TaloTheme.colors.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(TaloTheme.spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = TaloTheme.typography.body, color = TaloTheme.colors.primaryText)
            Text(text = subtitle, style = TaloTheme.typography.bodySmall, color = TaloTheme.colors.secondaryText)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TaloTheme.colors.tertiaryText)
    }
}
