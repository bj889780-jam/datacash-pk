package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppThemes {
    val darkTheme: ColorScheme = darkColorScheme(
        primary = EmeraldGreen,
        onPrimary = Color.White,
        primaryContainer = EmeraldGreenDark,
        onPrimaryContainer = Color.White,
        secondary = ActiveYellow,
        onSecondary = Color.Black,
        secondaryContainer = ActiveYellowDark,
        onSecondaryContainer = Color.Black,
        tertiary = AccentGold,
        background = DarkBackground,
        onBackground = Color(0xFFFFFFFF),
        surface = DarkSurface,
        onSurface = Color(0xFFFFFFFF),
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = Color(0xFFB0B0B0),
        outline = Color(0xFF2E2E2E),
        outlineVariant = Color(0xFF383838)
    )

    val lightTheme: ColorScheme = lightColorScheme(
        primary = EmeraldGreen,
        onPrimary = Color.White,
        primaryContainer = EmeraldGreenLight,
        onPrimaryContainer = EmeraldGreenDark,
        secondary = ActiveYellow,
        onSecondary = Color.Black,
        secondaryContainer = ActiveYellowLight,
        onSecondaryContainer = Color(0xFF78350F),
        tertiary = AccentGold,
        background = LightBackground,
        onBackground = Color(0xFF1A1A1A),
        surface = LightSurface,
        onSurface = Color(0xFF1A1A1A),
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = Color(0xFF6B7280),
        outline = BentoBorderLight,
        outlineVariant = Color(0xFFE2E8F0)
    )
}

/**
 * DataCash PK Theme
 * Automatically switches between lightTheme and darkTheme based on mobile system settings (themeMode = System).
 */
@Composable
fun DataCashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AppThemes.darkTheme else AppThemes.lightTheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

