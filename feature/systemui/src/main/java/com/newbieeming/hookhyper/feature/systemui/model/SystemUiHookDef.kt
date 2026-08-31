package com.newbieeming.hookhyper.feature.systemui.model

import com.newbieeming.hookhyper.core.ui.component.HookDef
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiCategory.LOCK_SCREEN
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiCategory.NOTIFICATION_BAR
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiCategory.STATUS_BAR
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiCategory.SUPER_ISLAND

/**
 * SystemUI 模块所有 Hook 的元数据定义。
 *
 * 每个条目集中声明偏好键名、所属分类和排列顺序，
 * 各 Hook 实现类通过引用对应条目获取这些属性。
 */
enum class SystemUiHookDef(
    override val preferenceKey: String,
    override val category: SystemUiCategory,
    override val order: Int,
) : HookDef {
    REPLACE_FINGERPRINT_ICON(
        preferenceKey = "systemui_replace_fingerprint_icon",
        category = LOCK_SCREEN,
        order = 0,
    ),
    LOCK_SHOW_SIM_NAME(
        preferenceKey = "systemui_lock_show_sim_name",
        category = LOCK_SCREEN,
        order = 1,
    ),
    CUSTOM_TIME_FORMAT(
        preferenceKey = "systemui_custom_time_format",
        category = STATUS_BAR,
        order = 0,
    ),
    FORCE_SOFT_LIGHT_GLASS(
        preferenceKey = "systemui_force_soft_light_glass",
        category = NOTIFICATION_BAR,
        order = 0,
    ),
    SUPER_ISLAND_DIMENSIONS(
        preferenceKey = "systemui_super_island_dimensions",
        category = SUPER_ISLAND,
        order = 0,
    ),

    /** 子选项，不属于 UI 列表，仅用于偏好读写 */
    TIME_FORMAT_AA_PREFIX(
        preferenceKey = "systemui_time_format_aa_prefix",
        category = STATUS_BAR,
        order = 1,
    ),
}
