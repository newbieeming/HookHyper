package com.newbieeming.hookhyper.core.ui.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.newbieeming.hookhyper.core.ui.component.HookContent

/**
 * [FeatureEntry] 的样板实现。
 *
 * 子类只需提供 Feature 信息和 [provideViewModel]，其余自动完成：
 * - [Content]：自动调用 [FeatureScreen]
 */
abstract class FeatureEntryImpl : FeatureEntry {

    /**
     * 通过 Hilt 创建当前 Feature 的 ViewModel。
     *
     * ```kotlin
     * @Composable
     * override fun provideViewModel(): FeatureViewModel = hiltViewModel<MyViewModel>()
     * ```
     */
    @Composable
    protected abstract fun provideViewModel(): FeatureViewModel

    abstract override val hooks: List<HookContent>

    @Composable
    override fun Content(onBack: () -> Unit, modifier: Modifier) {
        val context = LocalContext.current
        val title = remember(context, targetPackageName) {
            runCatching {
                context.packageManager.getApplicationInfo(targetPackageName, 0)
                    .let(context.packageManager::getApplicationLabel)
                    .toString()
            }.getOrDefault(targetPackageName)
        }
        FeatureScreen(
            viewModel = provideViewModel(),
            title = title,
            onBack = onBack,
            hooks = hooks,
            modifier = modifier,
        )
    }
}
