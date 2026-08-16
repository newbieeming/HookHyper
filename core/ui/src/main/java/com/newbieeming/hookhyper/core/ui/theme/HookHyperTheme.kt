package com.newbieeming.hookhyper.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.core.model.UiStyle
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLightColorScheme

val LocalUiStyle = staticCompositionLocalOf { UiStyle.MIUIX }

private val MiuiLightColors = lightColorScheme(
    primary = Color(0xFF3482FF),
    onPrimary = Color.White,
    background = Color(0xFFF5F5F7),
    surface = Color.White,
    surfaceVariant = Color(0xFFEDEEF2),
)

private val MiuiDarkColors = darkColorScheme(
    primary = Color(0xFF72A7FF),
    background = Color(0xFF0F0F11),
    surface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFF2A2A2D),
)

private val MiuiShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Composable
fun HookHyperTheme(
    style: UiStyle,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when (style) {
        UiStyle.MIUIX -> if (darkTheme) MiuiDarkColors else MiuiLightColors
        UiStyle.MATERIAL -> when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }
    }
    val themedContent: @Composable () -> Unit = {
        CompositionLocalProvider(LocalUiStyle provides style) {
            MaterialTheme(
                colorScheme = colorScheme,
                shapes = if (style == UiStyle.MIUIX) MiuiShapes else MaterialTheme.shapes,
                content = content,
            )
        }
    }
    if (style == UiStyle.MIUIX) {
        MiuixTheme(
            colors = if (darkTheme) miuixDarkColorScheme() else miuixLightColorScheme(),
            content = themedContent,
        )
    } else {
        themedContent()
    }
}
