package com.newbieeming.hookhyper.feature.systemui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.model.NetworkSpeedSettings

@Composable
internal fun NetworkSpeedPositionPreference(
    preferenceKey: String,
    @StringRes labelRes: Int,
    @StringRes hintRes: Int,
    allowNegative: Boolean,
) {
    val repository = LocalPreferencesRepository.current
    var value by remember(preferenceKey) {
        mutableStateOf(
            NetworkSpeedSettings.parsePosition(
                repository.getString(preferenceKey, NetworkSpeedSettings.DEFAULT_POSITION),
                allowNegative,
            ),
        )
    }
    val save: (Float) -> Unit = { next ->
        value = NetworkSpeedSettings.parsePosition(next.toString(), allowNegative)
        repository.putString(preferenceKey, value.toString())
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(labelRes),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = { save(0f) }, enabled = value != 0f) {
                    Text(stringResource(R.string.systemui_network_speed_reset_position))
                }
            }
            PositionStepper(value, allowNegative, save)
            Text(
                text = stringResource(hintRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PositionStepper(value: Float, allowNegative: Boolean, onValueChange: (Float) -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(
                onClick = { onValueChange(value - NetworkSpeedSettings.POSITION_STEP) },
                enabled = allowNegative || value > 0f,
            ) {
                Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.systemui_network_speed_decrease))
            }
            Text(
                text = stringResource(R.string.systemui_network_speed_position_value, value.toString()),
                modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            FilledTonalIconButton(onClick = { onValueChange(value + NetworkSpeedSettings.POSITION_STEP) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.systemui_network_speed_increase))
            }
        }
    }
}
