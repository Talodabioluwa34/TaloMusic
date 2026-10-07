package com.talodabi.talomusic.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.talodabi.talomusic.ui.theme.TaloTheme

@Composable
fun TrackSlider(
    currentPositionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val currentProgress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayProgress = if (isDragging) dragProgress else currentProgress

    val elapsedText = remember(isDragging, dragProgress, currentPositionMs, durationMs) {
        val ms = if (isDragging) (dragProgress * durationMs).toLong() else currentPositionMs
        val totalSeconds = (ms / 1000).coerceAtLeast(0)
        String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    val durationText = remember(durationMs) {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = displayProgress,
            onValueChange = {
                isDragging = true
                dragProgress = it
            },
            onValueChangeFinished = {
                isDragging = false
                val targetMs = (dragProgress * durationMs).toLong()
                onSeekTo(targetMs)
            },
            colors = SliderDefaults.colors(
                thumbColor = TaloTheme.colors.primary,
                activeTrackColor = TaloTheme.colors.primary,
                inactiveTrackColor = TaloTheme.colors.divider
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = elapsedText,
                style = TaloTheme.typography.caption,
                color = TaloTheme.colors.secondaryText
            )
            Text(
                text = durationText,
                style = TaloTheme.typography.caption,
                color = TaloTheme.colors.secondaryText
            )
        }
    }
}
