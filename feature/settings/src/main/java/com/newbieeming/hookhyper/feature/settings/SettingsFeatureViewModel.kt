package com.newbieeming.hookhyper.feature.settings

import android.content.Context
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository
import com.newbieeming.hookhyper.core.data.RootAppRestarter
import com.newbieeming.hookhyper.core.ui.feature.FeatureViewModel
import com.newbieeming.hookhyper.feature.settings.model.DeviceInfoFields
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsFeatureState(
    val values: Map<String, String> = emptyMap(),
)

sealed interface SettingsFeatureIntent {
    data class UpdateValue(val key: String, val value: String) : SettingsFeatureIntent
}

@HiltViewModel
class SettingsFeatureViewModel @Inject constructor(
    @ApplicationContext context: Context,
    preferences: HookPreferencesRepository,
    appRestarter: RootAppRestarter,
) : FeatureViewModel(context, preferences, appRestarter) {

    override val packageName = SettingsFeatureEntry.PACKAGE_NAME
    override val restartSuccessMessage = context.getString(R.string.restart_settings_success)

    private val _state = MutableStateFlow(
        SettingsFeatureState(
            values = DeviceInfoFields.all.associate {
                it.preferenceKey to preferences.getString(it.preferenceKey)
            },
        ),
    )
    val state: StateFlow<SettingsFeatureState> = _state.asStateFlow()

    fun accept(intent: SettingsFeatureIntent) {
        when (intent) {
            is SettingsFeatureIntent.UpdateValue -> {
                preferences.putString(intent.key, intent.value)
                _state.update { it.copy(values = it.values + (intent.key to intent.value)) }
            }
        }
    }
}
