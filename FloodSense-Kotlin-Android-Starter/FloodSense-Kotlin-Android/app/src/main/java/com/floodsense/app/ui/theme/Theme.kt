package com.floodsense.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.floodsense.app.data.ThemeMode

private val LightColors = lightColorScheme(
    primary = DeepBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = Teal,
    tertiary = Cyan,
    background = SurfaceBlue,
    surface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = PaleBlue,
    onBackground = Navy,
    onSurface = Navy,
    error = Danger
)

private val DarkColors = darkColorScheme(
    primary = Cyan,
    secondary = Teal,
    background = androidx.compose.ui.graphics.Color(0xFF071426),
    surface = androidx.compose.ui.graphics.Color(0xFF0B203A),
    onBackground = androidx.compose.ui.graphics.Color(0xFFF5FAFE),
    onSurface = androidx.compose.ui.graphics.Color(0xFFF5FAFE),
    error = androidx.compose.ui.graphics.Color(0xFFFF6B62)
)

@Composable
fun FloodSenseTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = FloodTypography,
        content = content
    )
}
