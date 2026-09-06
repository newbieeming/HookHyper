package com.newbieeming.hookhyper.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun HookHyperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: ThemeColor = ThemeColor.DYNAMIC,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        themeColor != ThemeColor.DYNAMIC -> themeColor.colorScheme(darkTheme)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        else -> ThemeColor.DEFAULT.colorScheme(darkTheme)
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
