package com.newbieeming.hookhyper.core.ui.feature

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository
import com.newbieeming.hookhyper.core.data.RestartAppResult
import com.newbieeming.hookhyper.core.data.RootAppRestarter
import com.newbieeming.hookhyper.core.ui.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 各 Feature 模块的 ViewModel 基类。
 *
 * 封装公共的重启逻辑与一次性 Effect 通道。
 * 子类通过 `@HiltViewModel` + `@Inject` 构造注入后，
 * 只需 override [packageName] 和 [restartSuccessMessage] 即可。
 */
open class FeatureViewModel(
    private val context: Context,
    val preferences: HookPreferencesRepository,
    private val appRestarter: RootAppRestarter,
) : ViewModel() {

    private val _isRestarting = MutableStateFlow(false)
    val isRestarting: StateFlow<Boolean> = _isRestarting.asStateFlow()

    private val _effects = Channel<String>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /** 子类 override 以提供目标应用包名 */
    protected open val packageName: String = ""

    /** 子类 override 以提供重启成功提示文案 */
    protected open val restartSuccessMessage: String = ""

    /** 执行重启，由 Screen 层调用 */
    fun onRestart() {
        if (_isRestarting.value) return
        _isRestarting.value = true
        viewModelScope.launch {
            val message = when (val result = appRestarter.restart(packageName)) {
                RestartAppResult.Success -> restartSuccessMessage
                RestartAppResult.RootRequired ->
                    context.getString(R.string.restart_root_required)
                is RestartAppResult.Failure ->
                    context.getString(R.string.restart_failed, result.reason)
            }
            _isRestarting.value = false
            _effects.send(message)
        }
    }
}
