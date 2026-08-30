package com.newbieeming.hookhyper.core.ui.mvi

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newbieeming.hookhyper.core.data.RestartAppResult
import com.newbieeming.hookhyper.core.data.RootAppRestarter
import com.newbieeming.hookhyper.core.ui.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<State : Any, Intent : Any, Effect : Any>(
    initialState: State,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<State> = mutableState.asStateFlow()

    private val effectChannel = Channel<Effect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun accept(intent: Intent) = onIntent(intent)

    protected abstract fun onIntent(intent: Intent)

    protected fun reduce(transform: State.() -> State) {
        mutableState.update(transform)
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    /**
     * 通用重启逻辑，管理 isRestarting 状态并发送结果 Effect。
     *
     * @param context 用于获取字符串资源
     * @param appRestarter 重启器
     * @param packageName 目标应用包名
     * @param isRestarting 当前是否已在重启中（防重入）
     * @param setRestarting 更新重启状态（true=开始，false=结束）
     * @param toEffect 消息字符串 → Effect 工厂
     * @param successMessage 重启成功时的提示
     */
    protected fun restartApp(
        context: Context,
        appRestarter: RootAppRestarter,
        packageName: String,
        isRestarting: Boolean,
        setRestarting: (Boolean) -> Unit,
        toEffect: (String) -> Effect,
        successMessage: String,
    ) {
        if (isRestarting) return
        setRestarting(true)
        viewModelScope.launch {
            val result = appRestarter.restart(packageName)
            setRestarting(false)
            sendEffect(
                toEffect(
                    when (result) {
                        RestartAppResult.Success -> successMessage
                        RestartAppResult.RootRequired ->
                            context.getString(R.string.restart_root_required)
                        is RestartAppResult.Failure ->
                            context.getString(R.string.restart_failed, result.reason)
                    },
                ),
            )
        }
    }
}
