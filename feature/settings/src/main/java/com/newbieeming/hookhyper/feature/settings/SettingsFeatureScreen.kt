package com.newbieeming.hookhyper.feature.settings

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.newbieeming.hookhyper.core.ui.component.FeatureScaffold
import com.newbieeming.hookhyper.core.ui.component.SettingSwitchRow
import com.newbieeming.hookhyper.feature.settings.model.DeviceInfoFields
import com.newbieeming.hookhyper.feature.settings.ui.DeviceInfoTextField
import com.newbieeming.hookhyper.feature.settings.ui.DeviceInfoTextFieldGroup

@Composable
fun SettingsFeatureScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsFeatureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val deviceInfoFields = DeviceInfoFields.all
    val focusRequesters = remember(deviceInfoFields.size) {
        List(deviceInfoFields.size) { FocusRequester() }
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.effects.collect { effect ->
            if (effect is SettingsFeatureEffect.ShowMessage) {
                snackbarHostState.showSnackbar(effect.message, withDismissAction = true)
            }
        }
    }
    FeatureScaffold(
        title = stringResource(R.string.feature_settings_name),
        onBack = onBack,
        onRestart = { viewModel.accept(SettingsFeatureIntent.RestartApp) },
        isRestarting = state.isRestarting,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    ) {
        SettingSwitchRow(
            title = stringResource(R.string.settings_edit_device_info_title),
            summary = stringResource(R.string.settings_edit_device_info_summary),
            checked = state.enabled,
            onCheckedChange = { viewModel.accept(SettingsFeatureIntent.SetEnabled(it)) },
        )
        AnimatedVisibility(
            visible = state.enabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            DeviceInfoTextFieldGroup {
                deviceInfoFields.forEachIndexed { index, field ->
                    val isLastField = index == deviceInfoFields.lastIndex
                    DeviceInfoTextField(
                        label = deviceInfoLabel(field.preferenceKey),
                        value = state.values[field.preferenceKey].orEmpty(),
                        onValueChange = {
                            viewModel.accept(
                                SettingsFeatureIntent.UpdateValue(
                                    field.preferenceKey,
                                    it
                                )
                            )
                        },
                        modifier = Modifier.focusRequester(focusRequesters[index]),
                        supportingText = field.originValue.takeIf { it.isNotBlank() }
                            ?.let { stringResource(R.string.device_info_origin_value, it) },
                        imeAction = if (isLastField) ImeAction.Done else ImeAction.Next,
                        onImeAction = {
                            if (isLastField) {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            } else {
                                focusRequesters[index + 1].requestFocus()
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun deviceInfoLabel(preferenceKey: String): String {
    val resourceId = when (preferenceKey) {
        "settings_device_name" -> R.string.device_name
        "settings_device_cpu_name" -> R.string.device_processor
        "settings_device_cpu_detail" -> R.string.device_processor_detail
        "settings_device_memory" -> R.string.device_memory
        "settings_device_battery" -> R.string.device_battery_capacity
        "settings_device_screen_resolution" -> R.string.device_resolution
        "settings_device_screen_size" -> R.string.device_screen_size
        "settings_device_os_version" -> R.string.device_os_version
        "settings_device_os_xms_version" -> R.string.device_xms_version
        "settings_device_os_ro_xms_version" -> R.string.device_ro_xms_version
        "settings_device_camera" -> R.string.device_camera
        "settings_device_baseband" -> R.string.device_baseband
        "settings_device_cert_model" -> R.string.device_cert_model
        "settings_device_hardware_version" -> R.string.device_hardware_version
        "settings_device_android_version" -> R.string.device_android_version
        "settings_device_kernel_version" -> R.string.device_kernel_version
        else -> error("Unsupported device info preference: $preferenceKey")
    }
    return stringResource(resourceId)
}
