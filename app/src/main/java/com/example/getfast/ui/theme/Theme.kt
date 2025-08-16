package com.example.getfast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    background = LightBackground,
    surface = SurfaceColor,
    error = ErrorColor,
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    error = ErrorColor,
)

/**
 * Stellt das Farb- und Typografie-Schema der App bereit.
 */
@Composable
fun GetFastTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
