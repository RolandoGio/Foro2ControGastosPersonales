package com.example.controlgastos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AeroAqua,
    onPrimary = AeroNight,
    secondary = AeroSky,
    onSecondary = AeroNight,
    tertiary = AeroGlow,
    onTertiary = AeroNight,
    background = AeroNight,
    onBackground = Color(0xFFEAFBFF),
    surface = AeroNightSurface,
    onSurface = Color(0xFFEAFBFF),
    surfaceVariant = Color(0xFF174E60),
    onSurfaceVariant = Color(0xFFC8EAF2),
    error = AeroError
)

private val LightColorScheme = lightColorScheme(
    primary = AeroWater,
    onPrimary = Color.White,
    secondary = AeroAqua,
    onSecondary = AeroDeep,
    tertiary = AeroGlow,
    onTertiary = AeroDeep,
    background = AeroMist,
    onBackground = AeroText,
    surface = AeroGlass,
    onSurface = AeroText,
    surfaceVariant = Color(0xFFD8F5FA),
    onSurfaceVariant = AeroTextSoft,
    error = AeroError
)

@Composable
fun ControlGastosPersonalesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
