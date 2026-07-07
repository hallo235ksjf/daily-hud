package com.halel.dailyhud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Palette lifted 1:1 from the web version's :root / [data-theme] CSS variables.
object HudPalette {
    val cyanDark = Color(0xFF22E5EE)
    val cyanDimDark = Color(0xFF0F8A91)
    val cyanLight = Color(0xFF0E97A1)
    val cyanDimLight = Color(0xFF0B757D)
    val amber = Color(0xFFFFB454)
    val danger = Color(0xFFFF5F6D)

    val bgDark = Color(0xFF070A0D)
    val bgAltDark = Color(0xFF0D1216)
    val panelDark = Color(0xFF11171C)
    val panel2Dark = Color(0xFF161D23)
    val textDark = Color(0xFFD9ECEF)
    val textDimDark = Color(0xFF728890)

    val bgLight = Color(0xFFEEF3F4)
    val bgAltLight = Color(0xFFE4EBEC)
    val panelLight = Color(0xFFFFFFFF)
    val panel2Light = Color(0xFFF4F8F9)
    val textLight = Color(0xFF10262A)
    val textDimLight = Color(0xFF5C7278)
}

data class HudColorSet(
    val bg: Color, val bgAlt: Color, val panel: Color, val panel2: Color,
    val text: Color, val textDim: Color, val cyan: Color, val cyanDim: Color,
    val danger: Color, val border: Color
)

val LocalHudColors = staticCompositionLocalOf {
    HudColorSet(
        bg = HudPalette.bgDark, bgAlt = HudPalette.bgAltDark, panel = HudPalette.panelDark,
        panel2 = HudPalette.panel2Dark, text = HudPalette.textDark, textDim = HudPalette.textDimDark,
        cyan = HudPalette.cyanDark, cyanDim = HudPalette.cyanDimDark, danger = HudPalette.danger,
        border = HudPalette.cyanDark.copy(alpha = 0.16f)
    )
}

/** Convenience accessor used from non-themed contexts like the ring activity. */
object HudColors {
    val bg get() = HudPalette.bgDark
    val text get() = HudPalette.textDark
    val cyan get() = HudPalette.cyanDark
}

@Composable
fun HudTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val colors = if (theme == AppTheme.DARK) {
        HudColorSet(
            bg = HudPalette.bgDark, bgAlt = HudPalette.bgAltDark, panel = HudPalette.panelDark,
            panel2 = HudPalette.panel2Dark, text = HudPalette.textDark, textDim = HudPalette.textDimDark,
            cyan = HudPalette.cyanDark, cyanDim = HudPalette.cyanDimDark, danger = HudPalette.danger,
            border = HudPalette.cyanDark.copy(alpha = 0.16f)
        )
    } else {
        HudColorSet(
            bg = HudPalette.bgLight, bgAlt = HudPalette.bgAltLight, panel = HudPalette.panelLight,
            panel2 = HudPalette.panel2Light, text = HudPalette.textLight, textDim = HudPalette.textDimLight,
            cyan = HudPalette.cyanLight, cyanDim = HudPalette.cyanDimLight, danger = HudPalette.danger,
            border = HudPalette.cyanLight.copy(alpha = 0.18f)
        )
    }

    val materialScheme = if (theme == AppTheme.DARK) {
        darkColorScheme(
            primary = colors.cyan, background = colors.bg, surface = colors.panel,
            onPrimary = colors.bg, onBackground = colors.text, onSurface = colors.text
        )
    } else {
        lightColorScheme(
            primary = colors.cyan, background = colors.bg, surface = colors.panel,
            onPrimary = colors.bg, onBackground = colors.text, onSurface = colors.text
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalHudColors provides colors) {
        MaterialTheme(colorScheme = materialScheme, content = content)
    }
}
