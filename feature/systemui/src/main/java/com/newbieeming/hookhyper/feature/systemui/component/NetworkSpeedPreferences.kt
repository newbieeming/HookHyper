package com.newbieeming.hookhyper.feature.systemui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.core.ui.component.SettingsPreferenceGroup
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.model.NetworkSpeedSettings
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiDimension

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
                SystemUiSliderPreference(
                    dimension = SystemUiDimension.FONT_SIZE,
                    labelRes = R.string.systemui_network_speed_font_size_label,
                    hintRes = R.string.systemui_network_speed_font_size_hint,
                )
                SingleLinePreference()
                SystemUiSliderPreference(
                    dimension = SystemUiDimension.VERTICAL_OFFSET,
                    labelRes = R.string.systemui_network_speed_vertical_offset_label,
                    hintRes = R.string.systemui_network_speed_vertical_offset_hint,
                )
                SystemUiSliderPreference(
                    dimension = SystemUiDimension.END_PADDING,
                    labelRes = R.string.systemui_network_speed_end_padding_label,
                    hintRes = R.string.systemui_network_speed_end_padding_hint,
                )
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
        SystemUiSliderPreference(
            dimension = SystemUiDimension.UNIT_GAP,
            labelRes = R.string.systemui_network_speed_unit_gap_label,
            hintRes = R.string.systemui_network_speed_unit_gap_hint,
        )
    }
}
