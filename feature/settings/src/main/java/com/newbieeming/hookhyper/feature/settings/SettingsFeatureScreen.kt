package com.newbieeming.hookhyper.feature.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.newbieeming.hookhyper.core.ui.component.HookFeatureScreen
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.feature.settings.hook.SettingsHookRegistry

@Composable
fun SettingsFeatureScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsFeatureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.effects.collect { effect ->
            if (effect is SettingsFeatureEffect.ShowMessage) {
                snackbarHostState.showSnackbar(effect.message, withDismissAction = true)
            }
        }
    }

    CompositionLocalProvider(
        LocalPreferencesRepository provides viewModel.preferences,
        LocalSettingsViewModel provides viewModel,
    ) {
        HookFeatureScreen(
            title = stringResource(R.string.feature_settings_name),
            onBack = onBack,
            onRestart = { viewModel.accept(SettingsFeatureIntent.RestartApp) },
            isRestarting = state.isRestarting,
            snackbarHostState = snackbarHostState,
            hooks = SettingsHookRegistry.hookContents,
            modifier = modifier,
        )
    }
}
