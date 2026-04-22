package com.gnaanaa.mtimer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val MTimerDarkColorScheme = darkColorScheme(
    primary = MTimerDarkPrimary,
    onPrimary = MTimerDarkOnPrimary,
    background = MTimerDarkBackground,
    surface = MTimerDarkSurface,
    onBackground = MTimerDarkOnBackground,
    onSurface = MTimerDarkOnSurface,
    secondary = MTimerDarkSecondary,
    outline = MTimerDarkOutline
)

val MTimerLightColorScheme = lightColorScheme(
    primary = MTimerLightPrimary,
    onPrimary = MTimerLightOnPrimary,
    background = MTimerLightBackground,
    surface = MTimerLightSurface,
    onBackground = MTimerLightOnBackground,
    onSurface = MTimerLightOnSurface,
    secondary = MTimerLightSecondary,
    outline = MTimerLightOutline
)

@Composable
fun MTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MTimerDarkColorScheme else MTimerLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
