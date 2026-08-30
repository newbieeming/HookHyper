package com.newbieeming.hookhyper.feature.settings.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.core.common.UiStyle
import com.newbieeming.hookhyper.core.ui.theme.LocalUiStyle
import com.newbieeming.hookhyper.feature.settings.R
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Duration.Companion.milliseconds
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField

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
    DeviceInfoTextFieldContent(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        supportingText = supportingText,
        imeAction = imeAction,
        onImeAction = onImeAction,
    )
}

@Composable
fun DeviceInfoTextFieldGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixCard(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxWidth(),
            insideMargin = PaddingValues(vertical = 6.dp),
            content = content,
        )
    } else {
        Surface(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(vertical = 6.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun DeviceInfoTextFieldContent(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(isFocused) {
        if (isFocused) {
            bringIntoViewRequester.bringIntoView()
            delay(IME_ANIMATION_SETTLE_MILLIS.milliseconds)
            bringIntoViewRequester.bringIntoView()
        }
    }
    val fieldModifier = modifier
        .fillMaxWidth()
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusChanged { isFocused = it.isFocused }
    val keyboardOptions = KeyboardOptions(imeAction = imeAction)
    val keyboardActions = KeyboardActions(
        onNext = { onImeAction() },
        onDone = { onImeAction() },
    )

    if (LocalUiStyle.current == UiStyle.MIUIX) {
        val fieldShape = RoundedCornerShape(16.dp)
        MiuixTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            backgroundColor = Color.Transparent,
            borderColor = Color.Transparent,
            cornerRadius = 16.dp,
            useLabelAsPlaceholder = true,
            singleLine = false,
            minLines = DEVICE_INFO_MIN_LINES,
            maxLines = DEVICE_INFO_MAX_LINES,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = fieldModifier.border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.outline,
                shape = fieldShape,
            ),
        )
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(stringResource(R.string.device_info_text_field_placeholder, label)) },
            supportingText = supportingText?.let { { Text(it) } },
            singleLine = false,
            minLines = DEVICE_INFO_MIN_LINES,
            maxLines = DEVICE_INFO_MAX_LINES,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = fieldModifier,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
            ),
        )
    }
}

private const val IME_ANIMATION_SETTLE_MILLIS = 300L
private const val DEVICE_INFO_MIN_LINES = 1
private const val DEVICE_INFO_MAX_LINES = 6
