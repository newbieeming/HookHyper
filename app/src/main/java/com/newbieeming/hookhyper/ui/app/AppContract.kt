package com.newbieeming.hookhyper.ui.app

import com.newbieeming.hookhyper.core.data.ModuleStatus

data class AppState(
    val predictiveBackEnabled: Boolean,
    val moduleStatus: ModuleStatus,
)

sealed interface AppIntent {
    data class SetPredictiveBackEnabled(val enabled: Boolean) : AppIntent
    data object RefreshModuleStatus : AppIntent
}

sealed interface AppEffect
