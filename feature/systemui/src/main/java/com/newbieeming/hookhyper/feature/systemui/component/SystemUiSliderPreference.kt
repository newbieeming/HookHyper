package com.newbieeming.hookhyper.feature.systemui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.model.NetworkSpeedSettings
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiDimension

@Composable
internal fun SystemUiSliderPreference(dimension: SystemUiDimension, @StringRes labelRes: Int, @StringRes hintRes: Int) {
    val repository = LocalPreferencesRepository.current
    var value by remember(dimension) {
        val enabled = dimension != SystemUiDimension.FONT_SIZE || repository.getBoolean(NetworkSpeedSettings.CUSTOM_FONT_SIZE)
        mutableStateOf(if (enabled) dimension.parse(repository.getString(dimension.preferenceKey)) else null)
    }
    val save: () -> Unit = {
        repository.putString(dimension.preferenceKey, value?.toString().orEmpty())
        if (dimension == SystemUiDimension.FONT_SIZE) {
            repository.putBoolean(NetworkSpeedSettings.CUSTOM_FONT_SIZE, value != null)
        }
    }
    val label = stringResource(labelRes)
    val displayValue = value?.let { stringResource(R.string.systemui_dimension_value, it.toString()) }
        ?: stringResource(R.string.systemui_dimension_default)
    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(displayValue, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                }
            }
            Slider(
                value = value ?: dimension.initialValue,
                onValueChange = { value = dimension.normalize(it) },
                onValueChangeFinished = save,
                valueRange = dimension.range,
                steps = dimension.sliderSteps,
                modifier = Modifier.fillMaxWidth().semantics {
                    contentDescription = label
                    stateDescription = displayValue
                },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(hintRes), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(
                    onClick = {
                        value = dimension.defaultValue
                        save()
                    },
                    enabled = value != dimension.defaultValue,
                ) {
                    Text(stringResource(R.string.systemui_dimension_reset))
                }
            }
        }
    }
}
