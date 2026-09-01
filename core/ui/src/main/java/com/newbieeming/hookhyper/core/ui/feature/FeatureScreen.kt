package com.newbieeming.hookhyper.core.ui.feature

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.newbieeming.hookhyper.core.ui.component.HookCategoryNavigation
import com.newbieeming.hookhyper.core.ui.component.HookContent
import com.newbieeming.hookhyper.core.ui.component.HookFeatureScreen
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository

/**
 * 各 Feature 模块的公共页面。
 *
 * 封装 ViewModel 状态收集、Snackbar effect 监听、
 * [LocalPreferencesRepository] 注入及 [HookFeatureScreen] 调用。
 * 各模块只需在 [com.newbieeming.hookhyper.core.ui.feature.FeatureEntry.Content]
 * 中创建 ViewModel 并调用此函数即可。
 *
 * @param viewModel 该功能模块的 [FeatureViewModel] 实例
 * @param title 页面标题
 * @param onBack 返回回调
 * @param hooks 该模块的 [HookContent] 列表
 * @param modifier Modifier
 */
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel,
    title: String,
    onBack: () -> Unit,
    onOpenCategory: (String) -> Unit,
    categoryId: String?,
    hooks: List<HookContent>,
    modifier: Modifier = Modifier,
) {
    val isRestarting by viewModel.isRestarting.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { message ->
            snackbarHostState.showSnackbar(message, withDismissAction = true)
        }
    }

    CompositionLocalProvider(
        LocalPreferencesRepository provides viewModel.preferences,
        LocalFeatureViewModel provides viewModel,
    ) {
        HookFeatureScreen(
            title = title,
            onBack = onBack,
            categoryNavigation = HookCategoryNavigation(
                categoryId = categoryId,
                onOpenCategory = onOpenCategory,
            ),
            onRestart = viewModel::onRestart,
            isRestarting = isRestarting,
            snackbarHostState = snackbarHostState,
            hooks = hooks,
            modifier = modifier,
        )
    }
}
