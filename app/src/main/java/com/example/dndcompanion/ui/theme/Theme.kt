package com.example.dndcompanion.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun createPergamentScheme(isRanger: Boolean) = lightColorScheme(
    primary = Waldgruen,
    onPrimary = PergamentHell,
    primaryContainer = WaldgruenHell,
    onPrimaryContainer = PergamentHell,

    secondary = Bronze,
    onSecondary = PergamentHell,
    secondaryContainer = BronzeHell,
    onSecondaryContainer = TintenSchwarz,

    tertiary = if (isRanger) OchsenblutRot else HexenLila,
    onTertiary = PergamentHell,
    tertiaryContainer = if (isRanger) OchsenblutRotHell else HexenLilaHell,
    onTertiaryContainer = PergamentHell,

    background = Pergament,
    onBackground = TintenSchwarz,

    surface = PergamentHell,
    onSurface = TintenSchwarz,
    surfaceVariant = PergamentDunkel,
    onSurfaceVariant = TintenBraun,

    outline = BronzeDunkel,
    outlineVariant = PergamentDunkel,

    error = TodRuneRot,
    onError = PergamentHell
)

@Composable
fun DnDCompanionTheme(
    isRanger: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = createPergamentScheme(isRanger)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = WaldgruenDunkel.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}