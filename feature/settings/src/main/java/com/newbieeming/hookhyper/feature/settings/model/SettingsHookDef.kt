package com.newbieeming.hookhyper.feature.settings.model

import com.newbieeming.hookhyper.core.ui.component.HookDef

/**
 * Settings 模块所有 Hook 的元数据定义。
 *
 * 每个条目集中声明偏好键名、所属分类和排列顺序，
 * 各 Hook 实现类通过引用对应条目获取这些属性。
 */
enum class SettingsHookDef(
    override val preferenceKey: String,
    override val category: SettingsCategory,
    override val order: Int,
) : HookDef {
    EDIT_DEVICE_INFO(
        preferenceKey = "settings_edit_device_info",
        category = SettingsCategory.DEVICE,
        order = 0,
    ),
}
