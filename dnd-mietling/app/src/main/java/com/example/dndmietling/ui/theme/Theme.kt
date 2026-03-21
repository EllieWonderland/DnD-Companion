package com.example.dndmietling.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PergamentColorScheme = lightColorScheme(
    primary = Waldgruen,
    onPrimary = Color.White,
    primaryContainer = WaldgruenHell,
    onPrimaryContainer = Color.White,
    secondary = Bronze,
    onSecondary = Color.White,
    secondaryContainer = BronzeHell,
    onSecondaryContainer = TintenSchwarz,
    tertiary = OchsenblutRot,
    onTertiary = Color.White,
    background = Pergament,
    onBackground = TintenSchwarz,
    surface = PergamentHell,
    onSurface = TintenSchwarz,
    surfaceVariant = PergamentDunkel,
    onSurfaceVariant = TintenBraun,
    outline = BronzeDunkel,
    error = OchsenblutRot,
    onError = Color.White
)

@Composable
fun DnDMietlingTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = WaldgruenDunkel.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = PergamentColorScheme,
        typography = Typography,
        content = content
    )
}
