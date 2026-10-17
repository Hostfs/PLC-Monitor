package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IndustrialColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF001E2B),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = OrangeSecondary,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF643F00),
    onSecondaryContainer = Color(0xFFFFDDB4),
    tertiary = StatusRunning,
    onTertiary = Color(0xFF00391A),
    background = IndustrialBackground,
    onBackground = TextPrimary,
    surface = IndustrialSurface,
    onSurface = TextPrimary,
    surfaceVariant = IndustrialSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = IndustrialSurfaceBorder,
    error = StatusCritical,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IndustrialColorScheme,
        typography = Typography,
        content = content
    )
}
