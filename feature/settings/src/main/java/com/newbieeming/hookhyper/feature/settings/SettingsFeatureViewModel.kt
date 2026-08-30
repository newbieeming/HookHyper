package com.newbieeming.hookhyper.feature.settings

import android.content.Context
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository
import com.newbieeming.hookhyper.core.data.RootAppRestarter
import com.newbieeming.hookhyper.core.ui.mvi.MviViewModel
import com.newbieeming.hookhyper.feature.settings.model.DeviceInfoFields
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class SettingsFeatureState(
    val values: Map<String, String> = emptyMap(),
    val isRestarting: Boolean = false,
)

sealed interface SettingsFeatureIntent {
    data class UpdateValue(val key: String, val value: String) : SettingsFeatureIntent
    data object RestartApp : SettingsFeatureIntent
}

sealed interface SettingsFeatureEffect {
    data class ShowMessage(val message: String) : SettingsFeatureEffect
}

@HiltViewModel
class SettingsFeatureViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    val preferences: HookPreferencesRepository,
    private val appRestarter: RootAppRestarter,
) : MviViewModel<SettingsFeatureState, SettingsFeatureIntent, SettingsFeatureEffect>(
    SettingsFeatureState(
        values = DeviceInfoFields.all.associate { it.preferenceKey to preferences.getString(it.preferenceKey) },
    ),
) {
    override fun onIntent(intent: SettingsFeatureIntent) {
        when (intent) {
            is SettingsFeatureIntent.UpdateValue -> {
                preferences.putString(intent.key, intent.value)
                reduce { copy(values = values + (intent.key to intent.value)) }
            }
            SettingsFeatureIntent.RestartApp -> {
                restartApp(
                    context = context,
                    appRestarter = appRestarter,
                    packageName = SettingsFeatureEntry.PACKAGE_NAME,
                    isRestarting = state.value.isRestarting,
                    setRestarting = { reduce { copy(isRestarting = it) } },
                    toEffect = { SettingsFeatureEffect.ShowMessage(it) },
                    successMessage = context.getString(R.string.restart_settings_success),
                )
            }
        }
    }
}
