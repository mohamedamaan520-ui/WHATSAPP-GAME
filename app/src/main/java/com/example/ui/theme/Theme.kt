package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF001A24),
    primaryContainer = Color(0xFF00384D),
    onPrimaryContainer = NeonCyan,
    secondary = WhatsAppGreen,
    onSecondary = Color(0xFF00220B),
    secondaryContainer = WhatsAppDark,
    onSecondaryContainer = WhatsAppGreen,
    tertiary = NeonOrange,
    onTertiary = Color(0xFF2A0D00),
    tertiaryContainer = Color(0xFF521C00),
    onTertiaryContainer = NeonOrange,
    background = CyberDark,
    onBackground = TextPrimary,
    surface = CyberDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberDarkCard,
    onSurfaceVariant = TextSecondary,
    outline = CyberDarkBorder,
    error = NeonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
