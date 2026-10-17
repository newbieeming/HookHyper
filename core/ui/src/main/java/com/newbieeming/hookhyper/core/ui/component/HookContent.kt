package com.newbieeming.hookhyper.core.ui.component

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable

/**
 * Hook 的 UI 元数据，用于声明所属分类并提供对应设置界面.
 */
interface HookContent {

    /** 控制此 hook 开关的偏好键名. */
    val preferenceKey: String

    /** 所属分类. */
    val category: HookCategory

    /** 同一分类中，相同展示类型内的顺序，值越小越靠前. */
    val order: Int

    /** 多项配置使用独立页面；仅有开关时保持 null，在分类列表直接展示. */
    val settingsPage: HookSettingsPage? get() = null

    /** 此 hook 的 UI 内容，包含主开关及子选项. */
    @Composable
    fun Content()
}

data class HookSettingsPage(
    @param:StringRes val titleResId: Int,
    @param:StringRes val summaryResId: Int,
)
