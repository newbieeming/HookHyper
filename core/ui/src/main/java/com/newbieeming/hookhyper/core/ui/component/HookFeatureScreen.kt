package com.newbieeming.hookhyper.core.ui.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * 通用 Hook 功能页面。
 *
 * 首页按 [HookCategory] 渲染可点击分类入口；二级页渲染该分类下的 Hook。
 *
 * @param title 页面标题
 * @param onBack 返回回调
 * @param categoryNavigation 分类二级页导航状态
 * @param onRestart 重启回调
 * @param isRestarting 是否正在重启
 * @param snackbarHostState Snackbar 状态
 * @param hooks 该功能模块的 HookContent 列表
 */
@Composable
fun HookFeatureScreen(
    title: String,
    onBack: () -> Unit,
    categoryNavigation: HookCategoryNavigation,
    onRestart: () -> Unit,
    isRestarting: Boolean,
    snackbarHostState: SnackbarHostState,
    hooks: List<HookContent>,
    modifier: Modifier = Modifier,
) {
    val categoryId = categoryNavigation.categoryId
    val grouped = remember(hooks) {
        hooks.groupBy { it.category }
            .toSortedMap(compareBy { it.order })
            .mapValues { (_, list) -> list.sortedBy { it.order } }
    }
    val selectedCategory = grouped.keys.firstOrNull { it.id == categoryId }
    val screenTitle = selectedCategory?.let { stringResource(it.titleResId) } ?: title

    LazyFeatureScaffold(
        title = screenTitle,
        onBack = onBack,
        onRestart = onRestart,
        isRestarting = isRestarting,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    ) {
        if (categoryId == null) {
            grouped.keys.forEach { category ->
                item(key = category.id, contentType = "category") {
                    HookCategoryPreference(
                        category = category,
                        onClick = { categoryNavigation.onOpenCategory(category.id) },
                    )
                }
            }
        } else {
            selectedCategory?.let(grouped::get).orEmpty().forEach { hook ->
                item(key = hook.preferenceKey, contentType = "hook") {
                    hook.Content()
                }
            }
        }
    }
}

data class HookCategoryNavigation(
    val categoryId: String?,
    val onOpenCategory: (String) -> Unit,
)
