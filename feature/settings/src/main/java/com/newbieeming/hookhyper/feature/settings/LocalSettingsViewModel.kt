package com.newbieeming.hookhyper.feature.settings

import androidx.compose.runtime.staticCompositionLocalOf

val LocalSettingsViewModel = staticCompositionLocalOf<SettingsFeatureViewModel> {
    error("No SettingsFeatureViewModel provided")
}
