package com.newbieeming.hookhyper.feature.settings.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.core.ui.component.PreferenceTextField
import com.newbieeming.hookhyper.core.ui.component.PreferenceTextFieldGroup
import com.newbieeming.hookhyper.feature.settings.R

@Composable
fun DeviceInfoTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    PreferenceTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        placeholder = { Text(stringResource(R.string.device_info_text_field_placeholder, label)) },
        supportingText = supportingText?.let { { Text(it) } },
        imeAction = imeAction,
        onImeAction = onImeAction,
    )
}

@Composable
fun DeviceInfoSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
fun DeviceInfoTextFieldGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    PreferenceTextFieldGroup(modifier = modifier, content = content)
}
