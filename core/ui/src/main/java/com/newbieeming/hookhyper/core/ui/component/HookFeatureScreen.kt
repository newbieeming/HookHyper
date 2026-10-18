package com.newbieeming.hookhyper.core.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.core.ui.R

/**
 * 通用 Hook 功能页面.
 *
 * 首页显示分类入口，分类页直接展示开关或配置入口，详情页展示单个 Hook 的配置.
 *
 * @param title 页面标题
 * @param onBack 返回回调
 * @param navigation 分类及功能详情的导航状态
 * @param onRestart 重启回调
 * @param isRestarting 是否正在重启
 * @param snackbarHostState Snackbar 状态
 * @param hooks 该功能模块的 HookContent 列表
 * @param modifier 页面修饰符
 */
@Composable
fun HookFeatureScreen(
    title: String,
    onBack: () -> Unit,
    navigation: HookFeatureNavigation,
    onRestart: () -> Unit,
    isRestarting: Boolean,
    snackbarHostState: SnackbarHostState,
    hooks: List<HookContent>,
    modifier: Modifier = Modifier,
) {
    val categoryId = navigation.categoryId
    val grouped = remember(hooks) {
        hooks.groupBy { it.category }
            .toSortedMap(compareBy { it.order })
            .mapValues { (_, list) ->
                list.sortedBy { it.order }
            }
    }
    val selectedCategory = grouped.keys.firstOrNull { it.id == categoryId }
    val categoryHooks = selectedCategory?.let(grouped::get).orEmpty()
    val selectedHook = categoryHooks.firstOrNull {
        it.preferenceKey == navigation.hookPreferenceKey && it.settingsPage != null
    }
    val screenTitle = selectedHook?.settingsPage?.let { stringResource(it.titleResId) }
        ?: selectedCategory?.let { stringResource(it.titleResId) } ?: title

    LazyFeatureScaffold(
        title = screenTitle,
        onBack = onBack,
        onRestart = onRestart,
        isRestarting = isRestarting,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    ) {
        if (navigation.hookPreferenceKey != null) {
            item(key = navigation.hookPreferenceKey, contentType = "hook_settings") {
                selectedHook?.Content() ?: Text(stringResource(R.string.hook_settings_unavailable))
            }
        } else if (categoryId == null) {
            grouped.keys.forEachIndexed { index, category ->
                item(key = category.id, contentType = "category") {
                    HookCategoryPreference(
                        category = category,
                        index = index,
                        count = grouped.size,
                        onClick = { navigation.onOpenCategory(category.id) },
                    )
                }
            }
        } else {
            val (pageHooks, inlineHooks) = categoryHooks.partition { it.settingsPage != null }
            hookItems(pageHooks, navigation)
            if (pageHooks.isNotEmpty() && inlineHooks.isNotEmpty()) {
                item(contentType = "group_spacing") {
                    Spacer(Modifier.height(12.dp))
                }
            }
            hookItems(inlineHooks, navigation)
        }
    }
}

private fun LazyListScope.hookItems(hooks: List<HookContent>, navigation: HookFeatureNavigation) {
    hooks.forEachIndexed { index, hook ->
        val page = hook.settingsPage
        item(key = hook.preferenceKey, contentType = if (page == null) "hook_switch" else "hook_navigation") {
            if (page == null) {
                CompositionLocalProvider(LocalSettingsPreferenceGroupShape provides settingsItemShape(index, hooks.size)) {
                    hook.Content()
                }
            } else {
                SettingsNavigationPreference(
                    title = stringResource(page.titleResId),
                    summary = stringResource(page.summaryResId),
                    onClick = { navigation.onOpenHook(hook.preferenceKey) },
                    index = index,
                    count = hooks.size,
                )
            }
        }
    }
}

data class HookFeatureNavigation(
    val categoryId: String? = null,
    val hookPreferenceKey: String? = null,
    val onOpenCategory: (String) -> Unit = {},
    val onOpenHook: (String) -> Unit = {},
)
