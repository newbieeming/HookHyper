package com.newbieeming.hookhyper.core.ui.component

import androidx.compose.runtime.Composable

/**
 * Hook 的 UI 元数据。每个 hook 实现此接口来声明所属分类并提供对应的设置界面。
 */
interface HookContent {

    /** 控制此 hook 开关的偏好键名 */
    val preferenceKey: String

    /** 所属分类 */
    val category: HookCategory

    /** 在同一分类内的显示顺序，值越小越靠前 */
    val order: Int

    /** 此 hook 的 UI 内容，包含主开关及子选项 */
    @Composable
    fun Content()
}
