package com.talodabi.talomusic.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalTaloColors = staticCompositionLocalOf { DarkTaloColors }
val LocalTaloTypography = staticCompositionLocalOf { TaloTypographyTokens() }

object TaloTheme {
    val colors: TaloColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTaloColors.current

    val typography: TaloTypographyTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalTaloTypography.current

    val spacing: TaloSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current

    val shapes: TaloShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current
}

@Composable
fun TaloMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val taloColors = if (darkTheme) DarkTaloColors else LightTaloColors
    val taloTypography = TaloTypographyTokens()
    val taloSpacing = TaloSpacing()
    val taloShapes = TaloShapes()

    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = taloColors.primary,
            onPrimary = taloColors.background,
            background = taloColors.background,
            onBackground = taloColors.primaryText,
            surface = taloColors.surface,
            onSurface = taloColors.primaryText,
            surfaceVariant = taloColors.surfaceElevated,
            onSurfaceVariant = taloColors.secondaryText,
            outline = taloColors.divider
        )
    } else {
        lightColorScheme(
            primary = taloColors.primary,
            onPrimary = LightSurface,
            background = taloColors.background,
            onBackground = taloColors.primaryText,
            surface = taloColors.surface,
            onSurface = taloColors.primaryText,
            surfaceVariant = taloColors.surfaceElevated,
            onSurfaceVariant = taloColors.secondaryText,
            outline = taloColors.divider
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = taloColors.background.toArgb()
                window.navigationBarColor = taloColors.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalTaloColors provides taloColors,
        LocalTaloTypography provides taloTypography,
        LocalSpacing provides taloSpacing,
        LocalShapes provides taloShapes
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = MaterialTypographyBridge,
            content = content
        )
    }
}
