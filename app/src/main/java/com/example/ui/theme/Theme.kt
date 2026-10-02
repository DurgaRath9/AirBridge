package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Cyan80,
    secondary = Indigo40,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF3730A3),
    onSecondaryContainer = Indigo80,
    tertiary = EmeraldAccent,
    background = DarkBase,
    onBackground = Color(0xFFF9FAFB),
    surface = DarkSurface,
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFFD1D5DB),
    outline = DarkOutline
)

private val AmoledColorScheme = DarkColorScheme.copy(
    background = AmoledBase,
    surface = AmoledBase,
    surfaceVariant = Color(0xFF121212)
)

private val LightColorScheme = lightColorScheme(
    primary = Cyan40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F4FF),
    onPrimaryContainer = Color(0xFF002026),
    secondary = Indigo40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = EmeraldAccent,
    background = LightBase,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightCard,
    onSurfaceVariant = Color(0xFF475569),
    outline = LightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoled: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        amoled && darkTheme -> AmoledColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
