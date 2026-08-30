package com.newbieeming.hookhyper.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository

/**
 * 自动读写 [HookPreferencesRepository] 的开关行。
 *
 * 内部管理 checked 状态，无需外部持有 State。
 *
 * @param preferenceKey 偏好键名
 * @param title 标题
 * @param summary 摘要
 * @param onCheckedChange 可选回调，开关变化时通知外部（用于子选项联动等场景）
 * @param modifier Modifier
 */
@Composable
fun HookSwitchPreference(
    preferenceKey: String,
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    val repo = LocalPreferencesRepository.current
    var checked by remember { mutableStateOf(repo.getBoolean(preferenceKey)) }
    SwitchPreference(
        title = title,
        summary = summary,
        checked = checked,
        onCheckedChange = {
            checked = it
            repo.putBoolean(preferenceKey, it)
            onCheckedChange?.invoke(it)
        },
        modifier = modifier,
    )
}
