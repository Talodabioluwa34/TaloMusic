package com.talodabi.talomusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.talodabi.talomusic.ui.theme.TaloTheme

@Composable
fun EqualizerDialog(
    onDismiss: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(true) }
    var bassLevel by remember { mutableFloatStateOf(6f) }
    var midLevel by remember { mutableFloatStateOf(0f) }
    var trebleLevel by remember { mutableFloatStateOf(3f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Equalizer", style = TaloTheme.typography.h2)
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TaloTheme.colors.primary, checkedTrackColor = TaloTheme.colors.primarySoft)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Acoustic Tuning",
                    style = TaloTheme.typography.caption,
                    color = TaloTheme.colors.secondaryText
                )
                Spacer(modifier = Modifier.height(TaloTheme.spacing.md))

                // Bass
                Text(text = "Bass (${bassLevel.toInt()} dB)", style = TaloTheme.typography.bodySmall)
                Slider(
                    value = bassLevel,
                    onValueChange = { bassLevel = it },
                    valueRange = -10f..10f,
                    enabled = isEnabled,
                    colors = SliderDefaults.colors(thumbColor = TaloTheme.colors.primary, activeTrackColor = TaloTheme.colors.primary)
                )

                // Mids
                Text(text = "Mids (${midLevel.toInt()} dB)", style = TaloTheme.typography.bodySmall)
                Slider(
                    value = midLevel,
                    onValueChange = { midLevel = it },
                    valueRange = -10f..10f,
                    enabled = isEnabled,
                    colors = SliderDefaults.colors(thumbColor = TaloTheme.colors.primary, activeTrackColor = TaloTheme.colors.primary)
                )

                // Treble
                Text(text = "Treble (${trebleLevel.toInt()} dB)", style = TaloTheme.typography.bodySmall)
                Slider(
                    value = trebleLevel,
                    onValueChange = { trebleLevel = it },
                    valueRange = -10f..10f,
                    enabled = isEnabled,
                    colors = SliderDefaults.colors(thumbColor = TaloTheme.colors.primary, activeTrackColor = TaloTheme.colors.primary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TaloTheme.colors.primary)
            ) {
                Text("Done", color = TaloTheme.colors.background)
            }
        },
        containerColor = TaloTheme.colors.surfaceElevated
    )
}
