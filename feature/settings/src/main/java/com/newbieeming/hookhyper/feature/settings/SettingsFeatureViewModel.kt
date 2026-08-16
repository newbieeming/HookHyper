package com.newbieeming.hookhyper.feature.settings

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository
import com.newbieeming.hookhyper.core.data.RestartAppResult
import com.newbieeming.hookhyper.core.data.RootAppRestarter
import com.newbieeming.hookhyper.core.model.DeviceInfoFields
import com.newbieeming.hookhyper.core.model.PreferenceKeys
import com.newbieeming.hookhyper.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.launch

data class SettingsFeatureState(
    val enabled: Boolean = false,
    val values: Map<String, String> = emptyMap(),
    val isRestarting: Boolean = false,
)

sealed interface SettingsFeatureIntent {
    data class SetEnabled(val enabled: Boolean) : SettingsFeatureIntent
    data class UpdateValue(val key: String, val value: String) : SettingsFeatureIntent
    data object RestartApp : SettingsFeatureIntent
}

sealed interface SettingsFeatureEffect {
    data object PreferenceSaved : SettingsFeatureEffect
    data class ShowMessage(val message: String) : SettingsFeatureEffect
}

@HiltViewModel
class SettingsFeatureViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferences: HookPreferencesRepository,
    private val appRestarter: RootAppRestarter,
) : MviViewModel<SettingsFeatureState, SettingsFeatureIntent, SettingsFeatureEffect>(
    SettingsFeatureState(
        enabled = preferences.getBoolean(PreferenceKeys.SETTINGS_EDIT_DEVICE_INFO),
        values = DeviceInfoFields.all.associate { it.preferenceKey to preferences.getString(it.preferenceKey) },
    ),
) {
    override fun onIntent(intent: SettingsFeatureIntent) {
        when (intent) {
            is SettingsFeatureIntent.SetEnabled -> {
                preferences.putBoolean(PreferenceKeys.SETTINGS_EDIT_DEVICE_INFO, intent.enabled)
                reduce { copy(enabled = intent.enabled) }
            }
            is SettingsFeatureIntent.UpdateValue -> {
                preferences.putString(intent.key, intent.value)
                reduce { copy(values = values + (intent.key to intent.value)) }
            }
            SettingsFeatureIntent.RestartApp -> {
                restartApp()
                return
            }
        }
        sendEffect(SettingsFeatureEffect.PreferenceSaved)
    }

    private fun restartApp() {
        if (state.value.isRestarting) return
        reduce { copy(isRestarting = true) }
        viewModelScope.launch {
            val result = appRestarter.restart(SettingsFeatureEntry.PACKAGE_NAME)
            reduce { copy(isRestarting = false) }
            sendEffect(
                SettingsFeatureEffect.ShowMessage(
                    when (result) {
                        RestartAppResult.Success -> context.getString(R.string.restart_settings_success)
                        RestartAppResult.RootRequired -> context.getString(R.string.restart_root_required)
                        is RestartAppResult.Failure -> context.getString(R.string.restart_failed, result.reason)
                    },
                ),
            )
        }
    }
}
