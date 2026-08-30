package com.newbieeming.hookhyper.feature.systemui

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
import com.newbieeming.hookhyper.core.ui.component.HookContent
import com.newbieeming.hookhyper.core.ui.component.HookFeatureScreen
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.feature.systemui.hook.HookRegistry

@Composable
fun SystemUiScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SystemUiViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.effects.collect { effect ->
            if (effect is SystemUiEffect.ShowMessage) {
                snackbarHostState.showSnackbar(effect.message, withDismissAction = true)
            }
        }
    }

    val hooks: List<HookContent> = remember {
        HookRegistry.modules.filterIsInstance<HookContent>()
    }

    CompositionLocalProvider(LocalPreferencesRepository provides viewModel.preferences) {
        HookFeatureScreen(
            title = stringResource(R.string.feature_systemui_name),
            onBack = onBack,
            onRestart = { viewModel.accept(SystemUiIntent.RestartApp) },
            isRestarting = state.isRestarting,
            snackbarHostState = snackbarHostState,
            hooks = hooks,
            modifier = modifier,
        )
    }
}
