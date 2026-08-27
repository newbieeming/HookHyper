package com.newbieeming.hookhyper.ui.app

import com.newbieeming.hookhyper.core.common.FeatureMetadata
import com.newbieeming.hookhyper.core.common.UiStyle
import com.newbieeming.hookhyper.core.data.ModuleStatus

data class AppState(
    val uiStyle: UiStyle,
    val predictiveBackEnabled: Boolean,
    val moduleStatus: ModuleStatus,
    val features: List<FeatureMetadata>,
)

sealed interface AppIntent {
    data class SelectUiStyle(val style: UiStyle) : AppIntent
    data class SetPredictiveBackEnabled(val enabled: Boolean) : AppIntent
    data object RefreshModuleStatus : AppIntent
}

sealed interface AppEffect {
    data object UiStyleChanged : AppEffect
}
