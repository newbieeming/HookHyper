package com.newbieeming.hookhyper.feature.systemui.component

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.core.ui.component.PreferenceTextField
import com.newbieeming.hookhyper.core.ui.component.SettingsPreferenceGroup
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.model.NetworkSpeedSettings

@Composable
internal fun NetworkSpeedPreferences(preferenceKey: String) {
    val repository = LocalPreferencesRepository.current
    var enabled by remember { mutableStateOf(repository.getBoolean(preferenceKey)) }
    SettingsPreferenceGroup {
        HookSwitchPreference(
            preferenceKey = preferenceKey,
            title = stringResource(R.string.systemui_network_speed_title),
            summary = stringResource(R.string.systemui_network_speed_summary),
            onCheckedChange = { enabled = it },
        )
        AnimatedVisibility(
            visible = enabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column {
                NetworkSpeedFontSizePreference()
                SingleLinePreference()
                HookSwitchPreference(
                    preferenceKey = NetworkSpeedSettings.HIDE_UNIT,
                    title = stringResource(R.string.systemui_network_speed_hide_unit_title),
                    summary = stringResource(R.string.systemui_network_speed_hide_unit_summary),
                )
            }
        }
    }
}

@Composable
private fun NetworkSpeedFontSizePreference() {
    val repository = LocalPreferencesRepository.current
    var enabled by remember { mutableStateOf(repository.getBoolean(NetworkSpeedSettings.CUSTOM_FONT_SIZE)) }
    HookSwitchPreference(
        preferenceKey = NetworkSpeedSettings.CUSTOM_FONT_SIZE,
        title = stringResource(R.string.systemui_network_speed_font_size_title),
        summary = stringResource(R.string.systemui_network_speed_font_size_summary),
        onCheckedChange = { enabled = it },
    )
    AnimatedVisibility(visible = enabled) {
        NetworkSpeedDimensionInput(
            preferenceKey = NetworkSpeedSettings.FONT_SIZE,
            defaultValue = NetworkSpeedSettings.DEFAULT_FONT_SIZE,
            labelRes = R.string.systemui_network_speed_font_size_label,
            hintRes = R.string.systemui_network_speed_font_size_hint,
        )
    }
}

@Composable
private fun SingleLinePreference() {
    val repository = LocalPreferencesRepository.current
    var enabled by remember { mutableStateOf(repository.getBoolean(NetworkSpeedSettings.SINGLE_LINE)) }
    HookSwitchPreference(
        preferenceKey = NetworkSpeedSettings.SINGLE_LINE,
        title = stringResource(R.string.systemui_network_speed_single_line_title),
        summary = stringResource(R.string.systemui_network_speed_single_line_summary),
        onCheckedChange = { enabled = it },
    )
    AnimatedVisibility(visible = enabled) {
        NetworkSpeedDimensionInput(
            preferenceKey = NetworkSpeedSettings.UNIT_GAP,
            defaultValue = NetworkSpeedSettings.DEFAULT_UNIT_GAP,
            labelRes = R.string.systemui_network_speed_unit_gap_label,
            hintRes = R.string.systemui_network_speed_unit_gap_hint,
        )
    }
}

@Composable
private fun NetworkSpeedDimensionInput(
    preferenceKey: String,
    defaultValue: String,
    @StringRes labelRes: Int,
    @StringRes hintRes: Int,
) {
    val repository = LocalPreferencesRepository.current
    var value by remember(preferenceKey) { mutableStateOf(repository.getString(preferenceKey, defaultValue)) }
    PreferenceTextField(
        label = stringResource(labelRes),
        value = value,
        onValueChange = {
            value = it
            repository.putString(preferenceKey, it)
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        supportingText = { Text(stringResource(hintRes)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    )
}
