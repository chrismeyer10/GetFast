package com.example.getfast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    background = LightBackground,
    surface = SurfaceColor,
    error = ErrorColor,
)

/**
 * Stellt das Farb- und Typografie-Schema der App bereit.
 */
@Composable
fun GetFastTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
