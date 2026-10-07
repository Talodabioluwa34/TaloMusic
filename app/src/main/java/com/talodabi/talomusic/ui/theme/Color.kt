package com.talodabi.talomusic.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Primary Brand Colors
val TaloOrange = Color(0xFFFF8232)
val TaloOrangePressed = Color(0xFFE96F24)

// Light Theme Palette (Warm & Editorial)
val LightBackground = Color(0xFFFAF9F7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF3F1EE)
val LightPrimaryText = Color(0xFF171513)
val LightSecondaryText = Color(0xFF6F6A65)
val LightTertiaryText = Color(0xFF99928B)
val LightDivider = Color(0xFFE7E2DC)
val LightPrimarySoft = Color(0xFFFFF0E7)

// Dark Theme Palette (Hero Visual Experience)
val DarkBackground = Color(0xFF0A0A0A)
val DarkSurface = Color(0xFF141414)
val DarkSurfaceElevated = Color(0xFF1C1C1C)
val DarkSurfaceStrong = Color(0xFF242424)
val DarkPrimaryText = Color(0xFFF7F5F2)
val DarkSecondaryText = Color(0xFFA8A29D)
val DarkTertiaryText = Color(0xFF706B66)
val DarkDivider = Color(0xFF292929)
val DarkPrimarySoft = Color(0xFF2A1710)

@Immutable
data class TaloColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceStrong: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val divider: Color,
    val primary: Color,
    val primaryPressed: Color,
    val primarySoft: Color,
    val isDark: Boolean
)

val DarkTaloColors = TaloColors(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceElevated = DarkSurfaceElevated,
    surfaceStrong = DarkSurfaceStrong,
    primaryText = DarkPrimaryText,
    secondaryText = DarkSecondaryText,
    tertiaryText = DarkTertiaryText,
    divider = DarkDivider,
    primary = TaloOrange,
    primaryPressed = TaloOrangePressed,
    primarySoft = DarkPrimarySoft,
    isDark = true
)

val LightTaloColors = TaloColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceElevated = LightSurfaceElevated,
    surfaceStrong = LightSurfaceElevated,
    primaryText = LightPrimaryText,
    secondaryText = LightSecondaryText,
    tertiaryText = LightTertiaryText,
    divider = LightDivider,
    primary = TaloOrange,
    primaryPressed = TaloOrangePressed,
    primarySoft = LightPrimarySoft,
    isDark = false
)
