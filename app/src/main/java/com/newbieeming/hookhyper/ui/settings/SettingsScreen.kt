package com.newbieeming.hookhyper.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.BuildConfig
import com.newbieeming.hookhyper.R
import com.newbieeming.hookhyper.core.data.ModuleStatus
import com.newbieeming.hookhyper.core.ui.component.SwitchPreference
import com.newbieeming.hookhyper.ui.app.AppIntent
import com.newbieeming.hookhyper.ui.app.AppState
import com.newbieeming.hookhyper.ui.component.AdaptiveCard
import com.newbieeming.hookhyper.ui.component.AdaptiveText
import com.newbieeming.hookhyper.ui.component.ScreenScaffold

@Composable
fun SettingsScreen(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(Unit) { onIntent(AppIntent.RefreshModuleStatus) }
    ScreenScaffold(title = stringResource(R.string.settings_title)) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ModuleStatusCard(
                    status = state.moduleStatus,
                    onRefresh = { onIntent(AppIntent.RefreshModuleStatus) },
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.predictive_back_title),
                    summary = stringResource(R.string.predictive_back_summary),
                    checked = state.predictiveBackEnabled,
                    onCheckedChange = {
                        onIntent(AppIntent.SetPredictiveBackEnabled(it))
                    },
                )
            }
            item {
                AdaptiveCard(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    AdaptiveText(stringResource(R.string.about_title), emphasized = true)
                    AdaptiveText(stringResource(R.string.about_summary))
                    AboutLinkRow(
                        title = stringResource(R.string.about_github),
                        value = stringResource(R.string.about_github_value),
                        onClick = { uriHandler.openUri("https://github.com/newbieeming") },
                    )
                    AboutLinkRow(
                        title = stringResource(R.string.about_coolapk),
                        value = stringResource(R.string.about_coolapk_value),
                        onClick = { uriHandler.openUri("https://www.coolapk.com/u/641970") },
                    )
                    AdaptiveText(stringResource(R.string.about_app_version, BuildConfig.VERSION_NAME))
                }
            }
        }
    }
}

@Composable
private fun ModuleStatusCard(
    status: ModuleStatus,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AdaptiveCard(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AdaptiveText(stringResource(R.string.lsposed_connection), emphasized = true)
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.action_refresh),
                )
            }
        }
        AssistChip(
            onClick = onRefresh,
            label = { Text(rememberModuleStatusText(status)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = if (status.isConnected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            },
            colors = AssistChipDefaults.assistChipColors(),
        )
    }
}

@Composable
private fun AboutLinkRow(
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AdaptiveText(title, emphasized = true)
            AdaptiveText(value)
        }
        Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = stringResource(R.string.action_open_link, title),
        )
    }
}

@Composable
private fun rememberModuleStatusText(status: ModuleStatus): String = if (status.isConnected) {
    stringResource(
        R.string.module_connected,
        status.frameworkName,
        status.apiLevel?.toString() ?: "-",
    )
} else {
    stringResource(R.string.module_disconnected)
}
