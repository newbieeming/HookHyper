package com.newbieeming.hookhyper.feature.systemui

import android.content.Context
import com.newbieeming.hookhyper.core.data.HookPreferencesRepository
import com.newbieeming.hookhyper.core.data.RootAppRestarter
import com.newbieeming.hookhyper.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class SystemUiState(
    val isRestarting: Boolean = false,
)

sealed interface SystemUiIntent {
    data object RestartApp : SystemUiIntent
}

sealed interface SystemUiEffect {
    data class ShowMessage(val message: String) : SystemUiEffect
}

@HiltViewModel
class SystemUiViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    val preferences: HookPreferencesRepository,
    private val appRestarter: RootAppRestarter,
) : MviViewModel<SystemUiState, SystemUiIntent, SystemUiEffect>(SystemUiState()) {

    override fun onIntent(intent: SystemUiIntent) {
        when (intent) {
            SystemUiIntent.RestartApp -> {
                restartApp(
                    context = context,
                    appRestarter = appRestarter,
                    packageName = SystemUiFeatureEntry.PACKAGE_NAME,
                    isRestarting = state.value.isRestarting,
                    setRestarting = { reduce { copy(isRestarting = it) } },
                    toEffect = { SystemUiEffect.ShowMessage(it) },
                    successMessage = context.getString(R.string.restart_systemui_success),
                )
            }
        }
    }
}
