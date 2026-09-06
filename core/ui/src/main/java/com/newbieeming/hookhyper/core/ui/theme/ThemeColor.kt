package com.newbieeming.hookhyper.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

enum class ThemeColor(val id: String, private val seedValue: Long) {
    DYNAMIC("dynamic", 0xFF6750A4),
    DEFAULT("default", 0xFF6750A4),
    PINK("pink", 0xFFB3266E),
    RED("red", 0xFFBA1A1A),
    ORANGE("orange", 0xFF9A4600),
    AMBER("amber", 0xFF8A5000),
    YELLOW("yellow", 0xFF6F5900),
    LIME("lime", 0xFF536D00),
    GREEN("green", 0xFF006E1C),
    CYAN("cyan", 0xFF006A6A),
    LIGHT_BLUE("light_blue", 0xFF0061A4),
    BLUE("blue", 0xFF005AC1),
    INDIGO("indigo", 0xFF3F51B5),
    PURPLE("purple", 0xFF7B1FA2),
    DEEP_PURPLE("deep_purple", 0xFF512DA8),
    BLUE_GREY("blue_grey", 0xFF455A64),
    BROWN("brown", 0xFF795548),
    ;

    val seed: Color get() = Color(seedValue)

    companion object {
        fun fromId(id: String): ThemeColor = entries.firstOrNull { it.id == id } ?: DYNAMIC
    }
}

/** Builds a Material 3 semantic scheme from each selectable accent color. */
fun ThemeColor.colorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    val primary = if (dark) lerp(seed, Color.White, DarkPrimaryLighten) else seed
    val container = if (dark) lerp(seed, Color.Black, DarkContainerDarken) else lerp(seed, Color.White, LightContainerLighten)
    val onPrimary = readableOn(primary)
    val onContainer = readableOn(container)
    fun neutral(color: Color): Color = lerp(color, seed, SurfaceTintAmount)
    return base.copy(
        primary = primary, onPrimary = onPrimary,
        primaryContainer = container, onPrimaryContainer = onContainer,
        secondary = primary, onSecondary = onPrimary,
        secondaryContainer = container, onSecondaryContainer = onContainer,
        tertiary = primary, onTertiary = onPrimary,
        tertiaryContainer = container, onTertiaryContainer = onContainer,
        inversePrimary = if (dark) seed else lerp(seed, Color.White, DarkPrimaryLighten),
        surfaceTint = primary,
        background = neutral(base.background),
        onBackground = neutral(base.onBackground),
        surface = neutral(base.surface),
        onSurface = neutral(base.onSurface),
        surfaceVariant = neutral(base.surfaceVariant),
        onSurfaceVariant = neutral(base.onSurfaceVariant),
        surfaceDim = neutral(base.surfaceDim),
        surfaceBright = neutral(base.surfaceBright),
        surfaceContainerLowest = neutral(base.surfaceContainerLowest),
        surfaceContainerLow = neutral(base.surfaceContainerLow),
        surfaceContainer = neutral(base.surfaceContainer),
        surfaceContainerHigh = neutral(base.surfaceContainerHigh),
        surfaceContainerHighest = neutral(base.surfaceContainerHighest),
        outline = neutral(base.outline),
        outlineVariant = neutral(base.outlineVariant),
        inverseSurface = neutral(base.inverseSurface),
        inverseOnSurface = neutral(base.inverseOnSurface),
    )
}

private fun readableOn(background: Color): Color = if (background.luminance() > ContrastThreshold) Color.Black else Color.White

private const val DarkPrimaryLighten = 0.42f
private const val DarkContainerDarken = 0.55f
private const val LightContainerLighten = 0.86f
private const val SurfaceTintAmount = 0.05f
private const val ContrastThreshold = 0.45f
