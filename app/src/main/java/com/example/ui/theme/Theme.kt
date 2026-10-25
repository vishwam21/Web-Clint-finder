package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RobopixViolet,
    onPrimary = Color.White,
    primaryContainer = RobopixVioletContainer,
    onPrimaryContainer = RobopixVioletBright,
    secondary = RobopixVioletBright,
    onSecondary = RobopixBlack,
    secondaryContainer = RobopixSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = WhatsAppGreen,
    onTertiary = Color.White,
    background = RobopixBackground,
    onBackground = TextPrimary,
    surface = RobopixSurface,
    onSurface = TextPrimary,
    surfaceVariant = RobopixSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = RobopixBorder,
    outlineVariant = RobopixBorderLight,
    error = StatusDanger,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme(
    // ROBOPIX is a technical SaaS dark interface by default as requested: "Black, white, charcoal grey and subtle violet accents"
    primary = RobopixViolet,
    onPrimary = Color.White,
    primaryContainer = RobopixVioletContainer,
    onPrimaryContainer = RobopixVioletBright,
    secondary = RobopixVioletBright,
    onSecondary = RobopixBlack,
    secondaryContainer = RobopixSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = WhatsAppGreen,
    onTertiary = Color.White,
    background = RobopixBackground,
    onBackground = TextPrimary,
    surface = RobopixSurface,
    onSurface = TextPrimary,
    surfaceVariant = RobopixSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = RobopixBorder,
    outlineVariant = RobopixBorderLight,
    error = StatusDanger,
    onError = Color.White
)

@Composable
fun RobopixTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    RobopixTheme(content = content)
}
