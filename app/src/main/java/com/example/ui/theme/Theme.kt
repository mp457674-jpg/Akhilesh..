package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = DeepSpace,
    primaryContainer = CardNavyBorder,
    onPrimaryContainer = CyberCyan,
    secondary = NeonAmber,
    onSecondary = DeepSpace,
    secondaryContainer = CardNavy,
    onSecondaryContainer = ArcGold,
    tertiary = MatrixGreen,
    onTertiary = DeepSpace,
    background = DeepSpace,
    onBackground = TextPrimary,
    surface = DarkNavy,
    onSurface = TextPrimary,
    surfaceVariant = CardNavy,
    onSurfaceVariant = TextSecondary,
    outline = CardNavyBorder,
    outlineVariant = CyanGlow,
    error = WarningRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Stark HUD theme consistent
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
