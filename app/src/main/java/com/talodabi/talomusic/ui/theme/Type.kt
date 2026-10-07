package com.talodabi.talomusic.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

// Google Fonts provider for Manrope
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = 0 // In standard apps, certificates array or resource id
)

val ManropeFont = GoogleFont("Manrope")

val ManropeFamily = FontFamily(
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.ExtraBold)
)

// Fallback to SansSerif if offline or prior to font download
val TaloFontFamily = FontFamily.SansSerif

@Immutable
data class TaloTypographyTokens(
    val display: TextStyle = TextStyle(
        fontFamily = TaloFontFamily,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    val h1: TextStyle = TextStyle(
        fontFamily = TaloFontFamily,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    val h2: TextStyle = TextStyle(
        fontFamily = TaloFontFamily,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    val h3: TextStyle = TextStyle(
        fontFamily = TaloFontFamily,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    val body: TextStyle = TextStyle(
        fontFamily = TaloFontFamily,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    val bodySmall: TextStyle = TextStyle(
        fontFamily = TaloFontFamily,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    val label: TextStyle = TextStyle(
        fontFamily = TaloFontFamily,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = TaloFontFamily,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

val MaterialTypographyBridge = Typography(
    displayLarge = TaloTypographyTokens().display,
    headlineLarge = TaloTypographyTokens().h1,
    headlineMedium = TaloTypographyTokens().h2,
    headlineSmall = TaloTypographyTokens().h3,
    bodyLarge = TaloTypographyTokens().body,
    bodyMedium = TaloTypographyTokens().bodySmall,
    labelLarge = TaloTypographyTokens().label,
    labelSmall = TaloTypographyTokens().caption
)
