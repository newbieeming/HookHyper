package com.newbieeming.hookhyper.feature.systemui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.newbieeming.hookhyper.core.ui.component.FeatureScaffold
import com.newbieeming.hookhyper.core.ui.component.SettingSwitchRow

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
    FeatureScaffold(
        title = stringResource(R.string.feature_systemui_name),
        onBack = onBack,
        onRestart = { viewModel.accept(SystemUiIntent.RestartApp) },
        isRestarting = state.isRestarting,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    ) {
        SettingSwitchRow(
            title = stringResource(R.string.systemui_show_carrier_title),
            summary = stringResource(R.string.systemui_show_carrier_summary),
            checked = state.showCarrierOnLockScreen,
            onCheckedChange = { viewModel.accept(SystemUiIntent.SetShowCarrier(it)) },
        )
        SettingSwitchRow(
            title = stringResource(R.string.systemui_soft_light_glass_title),
            summary = stringResource(R.string.systemui_soft_light_glass_summary),
            checked = state.forceSoftLightGlass,
            onCheckedChange = { viewModel.accept(SystemUiIntent.SetSoftLightGlass(it)) },
        )
        SettingSwitchRow(
            title = stringResource(R.string.systemui_custom_time_format_title),
            summary = stringResource(R.string.systemui_custom_time_format_summary),
            checked = state.customTimeFormat,
            onCheckedChange = { viewModel.accept(SystemUiIntent.SetCustomTimeFormat(it)) },
        )
        AnimatedVisibility(
            visible = state.customTimeFormat,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            SettingSwitchRow(
                title = stringResource(R.string.systemui_aa_prefix_title),
                summary = stringResource(R.string.systemui_aa_prefix_summary),
                checked = state.aaPrefix,
                onCheckedChange = { viewModel.accept(SystemUiIntent.SetAaPrefix(it)) },
            )
        }
        SettingSwitchRow(
            title = stringResource(R.string.systemui_replace_fingerprint_icon_title),
            summary = stringResource(R.string.systemui_replace_fingerprint_icon_summary),
            checked = state.replaceFingerprintIcon,
            onCheckedChange = { viewModel.accept(SystemUiIntent.SetReplaceFingerprintIcon(it)) },
        )
    }
}
