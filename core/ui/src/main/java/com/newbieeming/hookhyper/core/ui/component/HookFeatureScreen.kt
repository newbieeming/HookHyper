package com.newbieeming.hookhyper.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * 通用 Hook 功能页面。
 *
 * 接收一组 [HookContent]，按 [HookCategory] 分组渲染，
 * 支持分类折叠与头部磁吸。各 feature 模块只需传入 hooks 列表即可。
 *
 * @param title 页面标题
 * @param onBack 返回回调
 * @param onRestart 重启回调
 * @param isRestarting 是否正在重启
 * @param snackbarHostState Snackbar 状态
 * @param hooks 该功能模块的 HookContent 列表
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HookFeatureScreen(
    title: String,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    isRestarting: Boolean,
    snackbarHostState: SnackbarHostState,
    hooks: List<HookContent>,
    modifier: Modifier = Modifier,
) {
    val grouped = remember(hooks) {
        hooks.groupBy { it.category }
            .toSortedMap(compareBy { it.order })
            .mapValues { (_, list) -> list.sortedBy { it.order } }
    }
    val expandedMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            grouped.keys.forEach { put(it.id, true) }
        }
    }

    LazyFeatureScaffold(
        title = title,
        onBack = onBack,
        onRestart = onRestart,
        isRestarting = isRestarting,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    ) {
        grouped.forEach { (category, hookList) ->
            val categoryId = category.id
            val expanded = expandedMap[categoryId] ?: true

            stickyHeader(key = "header_$categoryId", contentType = "header") {
                HookCategoryHeader(
                    title = stringResource(category.titleResId),
                    expanded = expanded,
                    onToggle = { expandedMap[categoryId] = !expanded },
                )
            }

            item(key = "content_$categoryId", contentType = "content") {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column {
                        hookList.forEach { hook -> key(hook.preferenceKey) { hook.Content() } }
                    }
                }
            }
        }
    }
}
