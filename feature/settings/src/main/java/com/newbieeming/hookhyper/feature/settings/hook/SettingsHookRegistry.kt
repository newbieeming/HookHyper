package com.newbieeming.hookhyper.feature.settings.hook

import com.newbieeming.hookhyper.core.ui.component.HookContent

/**
 * Settings 模块的 UI Hook 注册表。
 *
 * KSP 生成的 [com.newbieeming.hookhyper.feature.settings.hook.HookRegistry]
 * 用于 hook 运行时注册；此处提供 UI 层的 [HookContent] 列表，
 * 由 [com.newbieeming.hookhyper.feature.settings.SettingsFeatureScreen] 渲染。
 */
object SettingsHookRegistry {

    val hookContents: List<HookContent> = listOf(
        DeviceSettingsHookContent(),
    )
}
