package com.shahriar.vpn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ShahriarColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberBg,
    secondary = NeonMagenta,
    onSecondary = CyberBg,
    tertiary = NeonGreen,
    background = CyberBg,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceAlt,
    onSurfaceVariant = TextDim,
    error = NeonMagenta,
    outline = CyberGrid
)

@Composable
fun ShahriarTheme(content: @Composable () -> Unit) {
    // The app is cyberpunk — always dark. 😎
    isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = ShahriarColorScheme,
        typography = ShahriarTypography,
        content = content
    )
}
