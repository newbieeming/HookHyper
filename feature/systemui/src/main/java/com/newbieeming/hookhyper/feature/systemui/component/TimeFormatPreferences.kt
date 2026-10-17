package com.newbieeming.hookhyper.feature.systemui.component

import androidx.compose.animation.AnimatedVisibility
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
import com.newbieeming.hookhyper.feature.systemui.model.ClockFormat
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef
import com.newbieeming.hookhyper.feature.systemui.model.TimeFormatSettings

@Composable
internal fun TimeFormatPreferences(preferenceKey: String) {
    val repo = LocalPreferencesRepository.current
    var enabled by remember { mutableStateOf(repo.getBoolean(preferenceKey)) }
    val legacyPrefix = repo.getBoolean(SystemUiHookDef.TIME_FORMAT_AA_PREFIX.preferenceKey)
    var pattern by remember {
        mutableStateOf(repo.getString(TimeFormatSettings.PATTERN, if (legacyPrefix) "a HH:mm" else TimeFormatSettings.DEFAULT_PATTERN))
    }
    var english by remember { mutableStateOf(repo.getBoolean(TimeFormatSettings.ENGLISH_AM_PM)) }
    var fontSize by remember { mutableStateOf(repo.getString(TimeFormatSettings.FONT_SIZE)) }
    val formatter = remember(pattern, english) { runCatching { ClockFormat(pattern, english) }.getOrNull() }
    SettingsPreferenceGroup {
        HookSwitchPreference(
            preferenceKey = preferenceKey,
            title = stringResource(R.string.systemui_custom_time_format_title),
            summary = stringResource(R.string.systemui_custom_time_format_summary),
            onCheckedChange = { enabled = it },
        )
        AnimatedVisibility(visible = enabled) {
            Column {
                PreferenceTextField(
                    label = stringResource(R.string.systemui_time_pattern_title),
                    value = pattern,
                    onValueChange = {
                        pattern = it
                        repo.putString(TimeFormatSettings.PATTERN, it)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    supportingText = {
                        Column {
                            Text(stringResource(R.string.systemui_time_pattern_hint))
                            Text(
                                formatter?.let { stringResource(R.string.systemui_time_preview, it.format(System.currentTimeMillis())) }
                                    ?: stringResource(R.string.systemui_time_pattern_invalid),
                            )
                        }
                    },
                )
                HookSwitchPreference(
                    preferenceKey = TimeFormatSettings.ENGLISH_AM_PM,
                    title = stringResource(R.string.systemui_time_english_am_pm_title),
                    summary = stringResource(R.string.systemui_time_english_am_pm_summary),
                    onCheckedChange = { english = it },
                )
                HookSwitchPreference(
                    preferenceKey = TimeFormatSettings.NOTIFICATION_BAR,
                    title = stringResource(R.string.systemui_time_notification_bar_title),
                    summary = stringResource(R.string.systemui_time_notification_bar_summary),
                )
                PreferenceTextField(
                    label = stringResource(R.string.systemui_time_font_size_title),
                    value = fontSize,
                    onValueChange = {
                        fontSize = it
                        repo.putString(TimeFormatSettings.FONT_SIZE, it)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = { Text(stringResource(R.string.systemui_time_font_size_hint)) },
                )
            }
        }
    }
}
