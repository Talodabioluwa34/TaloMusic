package com.talodabi.talomusic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.talodabi.talomusic.ui.theme.TaloOrange

/**
 * Distinctive Talo "T" brand mark:
 * An architectural T symbol with harmonic acoustic pillars.
 */
@Composable
fun TaloLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tint: Color = TaloOrange
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Horizontal crossbar of T
        val barH = h * 0.16f
        val barY = h * 0.12f
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.12f, barY),
            size = Size(w * 0.76f, barH),
            cornerRadius = CornerRadius(barH / 2f, barH / 2f)
        )

        // Center main stem of T
        val stemW = w * 0.15f
        val stemX = (w - stemW) / 2f
        val stemY = barY + barH * 0.6f
        val stemH = h * 0.65f
        drawRoundRect(
            color = tint,
            topLeft = Offset(stemX, stemY),
            size = Size(stemW, stemH),
            cornerRadius = CornerRadius(stemW / 2f, stemW / 2f)
        )

        // Left acoustic resonance bar
        val flankW = w * 0.11f
        val leftX = w * 0.22f
        val flankY = h * 0.38f
        val flankH = h * 0.35f
        drawRoundRect(
            color = tint.copy(alpha = 0.75f),
            topLeft = Offset(leftX, flankY),
            size = Size(flankW, flankH),
            cornerRadius = CornerRadius(flankW / 2f, flankW / 2f)
        )

        // Right acoustic resonance bar
        val rightX = w * 0.78f - flankW
        drawRoundRect(
            color = tint.copy(alpha = 0.75f),
            topLeft = Offset(rightX, flankY),
            size = Size(flankW, flankH),
            cornerRadius = CornerRadius(flankW / 2f, flankW / 2f)
        )
    }
}
