package com.talodabi.talomusic.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class TaloSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,        // Default screen horizontal padding
    val xl: Dp = 24.dp,        // Section spacing
    val xxl: Dp = 32.dp,       // Section spacing upper
    val xxxl: Dp = 40.dp,
    val huge: Dp = 48.dp,      // Minimum practical touch target
    val colossal: Dp = 64.dp
)

val LocalSpacing = androidx.compose.runtime.staticCompositionLocalOf { TaloSpacing() }
