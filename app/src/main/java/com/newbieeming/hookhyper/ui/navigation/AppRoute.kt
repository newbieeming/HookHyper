package com.newbieeming.hookhyper.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object SettingsRoute : NavKey

@Serializable
data class FeatureRoute(val targetPackageName: String) : NavKey
