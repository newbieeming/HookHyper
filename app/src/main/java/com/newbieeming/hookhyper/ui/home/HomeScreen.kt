package com.newbieeming.hookhyper.ui.home

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.newbieeming.hookhyper.R
import com.newbieeming.hookhyper.core.common.FeatureMetadata
import com.newbieeming.hookhyper.core.common.UiStyle
import com.newbieeming.hookhyper.core.ui.theme.LocalUiStyle
import com.newbieeming.hookhyper.ui.component.ScreenScaffold
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun HomeScreen(
    features: List<FeatureMetadata>,
    onOpenFeature: (String) -> Unit,
) {
    ScreenScaffold(title = stringResource(R.string.app_name)) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(features, key = FeatureMetadata::id) { feature ->
                FeatureRow(feature = feature, onClick = { onOpenFeature(feature.id) })
            }
        }
    }
}

@SuppressLint("QueryPermissionsNeeded")
@Composable
private fun FeatureRow(feature: FeatureMetadata, onClick: () -> Unit) {
    val context = LocalContext.current
    val appInfo = remember(feature.packageName) {
        runCatching {
            val info: ApplicationInfo = context.packageManager.getApplicationInfo(feature.packageName, 0)
            context.packageManager.getApplicationLabel(info).toString() to
                context.packageManager.getApplicationIcon(info).toBitmap().asImageBitmap()
        }.getOrNull()
    }
    if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixCard(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            insideMargin = PaddingValues(16.dp),
            pressFeedbackType = PressFeedbackType.Sink,
            onClick = onClick,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FeatureIcon(appInfo)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    MiuixText(appInfo?.first ?: feature.fallbackName, fontWeight = FontWeight.SemiBold)
                    MiuixText(feature.packageName)
                }
            }
        }
    } else {
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp,
            onClick = onClick,
        ) {
            ListItem(
                headlineContent = { Text(appInfo?.first ?: feature.fallbackName, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(feature.packageName, style = MaterialTheme.typography.labelMedium) },
                leadingContent = { FeatureIcon(appInfo) },
            )
        }
    }
}

@Composable
private fun FeatureIcon(appInfo: Pair<String, ImageBitmap>?) {
    if (appInfo != null) {
        Image(appInfo.second, contentDescription = null, modifier = Modifier.size(48.dp))
    } else if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixIcon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(48.dp))
    } else {
        Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(48.dp))
    }
}
