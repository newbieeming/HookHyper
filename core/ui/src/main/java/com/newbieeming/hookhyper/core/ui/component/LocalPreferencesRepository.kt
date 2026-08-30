package com.newbieeming.hookhyper.core.ui.component

import androidx.compose.runtime.staticCompositionLocalOf
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository

val LocalPreferencesRepository = staticCompositionLocalOf<HookPreferencesRepository> {
    error("No HookPreferencesRepository provided")
}
