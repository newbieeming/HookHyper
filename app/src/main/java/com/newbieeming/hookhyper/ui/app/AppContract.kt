package com.newbieeming.hookhyper.ui.app

import com.newbieeming.hookhyper.core.data.ModuleStatus
import com.newbieeming.hookhyper.core.ui.theme.ThemeColor

data class AppState(
    val predictiveBackEnabled: Boolean,
    val moduleStatus: ModuleStatus,
    val themeColor: ThemeColor = ThemeColor.DYNAMIC,
)

sealed interface AppIntent {
    data class SetThemeColor(val color: ThemeColor) : AppIntent
    data class SetPredictiveBackEnabled(val enabled: Boolean) : AppIntent
    data object RefreshModuleStatus : AppIntent
}

sealed interface AppEffect
