package com.newbieeming.hookhyper.feature.systemui

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository
import com.newbieeming.hookhyper.core.data.RestartAppResult
import com.newbieeming.hookhyper.core.data.RootAppRestarter
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiPreferenceKeys
import com.newbieeming.hookhyper.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.launch

data class SystemUiState(
    val showCarrierOnLockScreen: Boolean = false,
    val forceSoftLightGlass: Boolean = false,
    val customTimeFormat: Boolean = false,
    val aaPrefix: Boolean = false,
    val isRestarting: Boolean = false,
)

sealed interface SystemUiIntent {
    data class SetShowCarrier(val enabled: Boolean) : SystemUiIntent
    data class SetSoftLightGlass(val enabled: Boolean) : SystemUiIntent
    data class SetCustomTimeFormat(val enabled: Boolean) : SystemUiIntent
    data class SetAaPrefix(val enabled: Boolean) : SystemUiIntent
    data object RestartApp : SystemUiIntent
}

sealed interface SystemUiEffect {
    data object PreferenceSaved : SystemUiEffect
    data class ShowMessage(val message: String) : SystemUiEffect
}

@HiltViewModel
class SystemUiViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferences: HookPreferencesRepository,
    private val appRestarter: RootAppRestarter,
) : MviViewModel<SystemUiState, SystemUiIntent, SystemUiEffect>(
    SystemUiState(
        showCarrierOnLockScreen = preferences.getBoolean(SystemUiPreferenceKeys.LOCK_SHOW_SIM_NAME),
        forceSoftLightGlass = preferences.getBoolean(SystemUiPreferenceKeys.FORCE_SOFT_LIGHT_GLASS),
        customTimeFormat = preferences.getBoolean(SystemUiPreferenceKeys.CUSTOM_TIME_FORMAT),
        aaPrefix = preferences.getBoolean(SystemUiPreferenceKeys.TIME_FORMAT_AA_PREFIX),
    ),
) {
    override fun onIntent(intent: SystemUiIntent) {
        when (intent) {
            is SystemUiIntent.SetShowCarrier -> {
                preferences.putBoolean(SystemUiPreferenceKeys.LOCK_SHOW_SIM_NAME, intent.enabled)
                reduce { copy(showCarrierOnLockScreen = intent.enabled) }
            }
            is SystemUiIntent.SetSoftLightGlass -> {
                preferences.putBoolean(SystemUiPreferenceKeys.FORCE_SOFT_LIGHT_GLASS, intent.enabled)
                reduce { copy(forceSoftLightGlass = intent.enabled) }
            }
            is SystemUiIntent.SetCustomTimeFormat -> {
                preferences.putBoolean(SystemUiPreferenceKeys.CUSTOM_TIME_FORMAT, intent.enabled)
                reduce { copy(customTimeFormat = intent.enabled) }
            }
            is SystemUiIntent.SetAaPrefix -> {
                preferences.putBoolean(SystemUiPreferenceKeys.TIME_FORMAT_AA_PREFIX, intent.enabled)
                reduce { copy(aaPrefix = intent.enabled) }
            }
            SystemUiIntent.RestartApp -> {
                restartApp()
                return
            }
        }
        sendEffect(SystemUiEffect.PreferenceSaved)
    }

    private fun restartApp() {
        if (state.value.isRestarting) return
        reduce { copy(isRestarting = true) }
        viewModelScope.launch {
            val result = appRestarter.restart(SystemUiFeatureEntry.PACKAGE_NAME)
            reduce { copy(isRestarting = false) }
            sendEffect(
                SystemUiEffect.ShowMessage(
                    when (result) {
                        RestartAppResult.Success -> context.getString(R.string.restart_systemui_success)
                        RestartAppResult.RootRequired -> context.getString(R.string.restart_root_required)
                        is RestartAppResult.Failure -> context.getString(R.string.restart_failed, result.reason)
                    },
                ),
            )
        }
    }
}
