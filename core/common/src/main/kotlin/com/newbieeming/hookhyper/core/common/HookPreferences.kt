package com.newbieeming.hookhyper.core.common

enum class UiStyle {
    MIUIX,
    MATERIAL,
}

/** Shared preference file and application-wide preference keys. */
object PreferenceKeys {
    const val FILE_NAME = "hookhyper_prefs"
    const val UI_STYLE = "ui_style"
    const val PREDICTIVE_BACK_ENABLED = "predictive_back_enabled"
}
