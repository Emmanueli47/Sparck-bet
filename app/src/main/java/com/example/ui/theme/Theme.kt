package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PulseEmerald,
    onPrimary = Color.Black,
    primaryContainer = PulseEmeraldDark,
    onPrimaryContainer = Color.White,
    secondary = PulseCyan,
    onSecondary = Color.Black,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = PulseCyan,
    tertiary = PulseAmber,
    onTertiary = Color.Black,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderStroke,
    error = PulseCrimson,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PulseEmeraldDark,
    onPrimary = Color.White,
    secondary = PulseCyan,
    tertiary = PulseAmber,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    outline = Color(0xFFCBD5E1),
    error = PulseCrimson
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek sports dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
