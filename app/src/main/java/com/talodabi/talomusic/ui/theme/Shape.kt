package com.talodabi.talomusic.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class TaloShapes(
    val small: Shape = RoundedCornerShape(8.dp),
    val medium: Shape = RoundedCornerShape(12.dp),
    val large: Shape = RoundedCornerShape(16.dp),
    val hero: Shape = RoundedCornerShape(20.dp),
    val circular: Shape = CircleShape
)

val LocalShapes = androidx.compose.runtime.staticCompositionLocalOf { TaloShapes() }
