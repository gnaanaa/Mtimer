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
    primary = Color(0xFF1F6F8B),           // Ocean Blue
    onPrimary = Color(0xFFFFFFFF),

    primaryContainer = Color(0xFF99C1DE),  // Soft Aqua
    onPrimaryContainer = Color(0xFF0F3D5E),

    secondary = Color(0xFFC9ADA7),         // Driftwood
    onSecondary = Color(0xFF1A1A2E),

    secondaryContainer = Color(0xFFF2E9E4), // Sand Beige
    onSecondaryContainer = Color(0xFF1A1A2E),

    tertiary = Color(0xFFE76F51),          // Coral Accent
    onTertiary = Color(0xFFFFFFFF),

    tertiaryContainer = Color(0xFFFFDAD4),
    onTertiaryContainer = Color(0xFF5A1A12),

    background = Color(0xFFF2E9E4),        // Sand
    onBackground = Color(0xFF1A1A2E),

    surface = Color(0xFFFAFAFA),           // Foam
    onSurface = Color(0xFF1A1A2E),

    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF4A4A4A),

    outline = Color(0xFF9A8C98),           // Warm Taupe

    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF)
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
