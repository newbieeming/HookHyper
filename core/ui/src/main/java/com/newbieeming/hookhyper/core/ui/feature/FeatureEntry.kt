package com.newbieeming.hookhyper.core.ui.feature

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.newbieeming.hookhyper.core.ui.component.HookContent

interface FeatureEntry {
    /** 此 Feature 对应的目标应用包名，同时作为导航 key。 */
    val targetPackageName: String

    /** 该功能模块的 HookContent 列表，用于 UI 渲染 */
    val hooks: List<HookContent>

    @Composable
    fun Content(
        onBack: () -> Unit,
        onOpenCategory: (String) -> Unit,
        categoryId: String?,
        modifier: Modifier,
    )
}
