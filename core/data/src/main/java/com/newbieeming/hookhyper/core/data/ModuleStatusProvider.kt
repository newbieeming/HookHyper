package com.newbieeming.hookhyper.core.data

import com.highcapable.yukihookapi.YukiHookAPI
import javax.inject.Inject

data class ModuleStatus(
    val isConnected: Boolean,
    val frameworkName: String,
    val apiLevel: Int?,
)

class ModuleStatusProvider @Inject constructor() {
    fun current(): ModuleStatus = runCatching {
        val connected = YukiHookAPI.Status.isModuleActive
        ModuleStatus(
            isConnected = connected,
            frameworkName = if (connected) YukiHookAPI.Status.Executor.name else "",
            apiLevel = if (connected) YukiHookAPI.Status.Executor.apiLevel else null,
        )
    }.getOrElse {
        ModuleStatus(isConnected = false, frameworkName = "", apiLevel = null)
    }
}
