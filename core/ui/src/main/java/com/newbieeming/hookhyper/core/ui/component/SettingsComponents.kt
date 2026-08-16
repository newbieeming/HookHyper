package com.newbieeming.hookhyper.core.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.newbieeming.hookhyper.core.model.UiStyle
import com.newbieeming.hookhyper.core.ui.R
import com.newbieeming.hookhyper.core.ui.theme.LocalUiStyle
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureScaffold(
    title: String,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    isRestarting: Boolean,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixScaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                SystemSettingsTopBar(
                    title = title,
                    navigationIcon = {
                        MiuixIconButton(onClick = onBack) {
                            MiuixIcon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    },
                    actions = {
                        MiuixIconButton(onClick = onRestart, enabled = !isRestarting) {
                            RestartActionContent(isRestarting)
                        }
                    },
                )
            },
        ) { padding ->
            FeatureContent(padding, content)
        }
    } else {
        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                SystemSettingsTopBar(
                    title = title,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onRestart, enabled = !isRestarting) {
                            RestartActionContent(isRestarting)
                        }
                    },
                )
            },
        ) { padding ->
            FeatureContent(padding, content)
        }
    }
}

@Composable
fun SystemSettingsTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val hasNavigation = navigationIcon != null
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(64.dp)
                .padding(start = if (hasNavigation) 4.dp else 24.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationIcon?.invoke()
            if (LocalUiStyle.current == UiStyle.MIUIX) {
                MiuixText(
                    text = title,
                    modifier = Modifier.weight(1f),
                    fontSize = if (hasNavigation) 22.sp else 28.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = if (hasNavigation) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.headlineMedium
                    },
                    fontWeight = FontWeight.SemiBold,
                )
            }
            actions()
        }
    }
}

@Composable
private fun RestartActionContent(isRestarting: Boolean) {
    if (isRestarting) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
        )
    } else if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixIcon(
            Icons.Default.RestartAlt,
            contentDescription = stringResource(R.string.action_restart_app),
        )
    } else {
        Icon(
            Icons.Default.RestartAlt,
            contentDescription = stringResource(R.string.action_restart_app),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeatureContent(
    padding: PaddingValues,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBringIntoViewSpec provides CenteredBringIntoViewSpec) {
        val density = LocalDensity.current
        val isImeVisible = WindowInsets.ime.getBottom(density) > 0
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .fillMaxSize(),
        ) {
            val focusedFieldTrailingSpace = maxHeight / 2
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                content()
                if (isImeVisible) {
                    Spacer(Modifier.height(focusedFieldTrailingSpace))
                }
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixCard(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxWidth(),
            insideMargin = PaddingValues(0.dp),
        ) {
            SwitchPreference(
                modifier = Modifier.fillMaxWidth(),
                title = title,
                summary = summary,
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    } else {
        Surface(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 1.dp,
        ) {
            ListItem(
                headlineContent = { Text(title) },
                supportingContent = { Text(summary) },
                trailingContent = {
                    Switch(checked = checked, onCheckedChange = onCheckedChange)
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@Composable
fun SettingTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    grouped: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    if (grouped) {
        SettingTextFieldContent(
            label = label,
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            imeAction = imeAction,
            onImeAction = onImeAction,
        )
        return
    }

    if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixCard(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxWidth(),
            insideMargin = PaddingValues(12.dp),
        ) {
            SettingTextFieldContent(
                label = label,
                value = value,
                onValueChange = onValueChange,
                imeAction = imeAction,
                onImeAction = onImeAction,
            )
        }
    } else {
        Surface(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 1.dp,
        ) {
            SettingTextFieldContent(
                label = label,
                value = value,
                onValueChange = onValueChange,
                imeAction = imeAction,
                onImeAction = onImeAction,
            )
        }
    }
}

@Composable
fun SettingTextFieldGroup(
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
private fun SettingTextFieldContent(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(isFocused) {
        if (isFocused) {
            bringIntoViewRequester.bringIntoView()
            delay(IME_ANIMATION_SETTLE_MILLIS)
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
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = fieldModifier
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) {
                        MiuixTheme.colorScheme.primary
                    } else {
                        MiuixTheme.colorScheme.outline
                    },
                    shape = fieldShape,
                ),
        )
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(stringResource(R.string.setting_text_field_placeholder, label)) },
            singleLine = true,
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

private object CenteredBringIntoViewSpec : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float =
        offset - (containerSize - size) / 2f
}

private const val IME_ANIMATION_SETTLE_MILLIS = 300L
