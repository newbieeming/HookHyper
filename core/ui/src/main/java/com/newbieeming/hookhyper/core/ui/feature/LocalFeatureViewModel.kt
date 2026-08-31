package com.newbieeming.hookhyper.core.ui.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/** 由 [FeatureScreen] 自动注入，供 hook UI 获取当前 feature 的 ViewModel。 */
val LocalFeatureViewModel = staticCompositionLocalOf<FeatureViewModel> {
    error("No FeatureViewModel provided")
}

/**
 * 类型安全地获取当前 feature 的 ViewModel。
 *
 * 在 hook 的 `Content()` 中使用：
 * ```kotlin
 * val viewModel = featureViewModel<SettingsFeatureViewModel>()
 * ```
 */
@Composable
@ReadOnlyComposable
inline fun <reified T : FeatureViewModel> featureViewModel(): T =
    LocalFeatureViewModel.current as T
