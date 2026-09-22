package com.newbieeming.hookhyper.core.data

import javax.inject.Inject

data class ModuleStatus(
    val isConnected: Boolean,
    val frameworkName: String,
    val apiLevel: Int?,
)

class ModuleStatusProvider @Inject constructor(preferences: HookPreferencesRepository) {
    val status = preferences.status

    fun current(): ModuleStatus = status.value
}
