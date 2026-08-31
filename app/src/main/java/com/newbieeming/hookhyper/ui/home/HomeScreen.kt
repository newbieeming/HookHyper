package com.newbieeming.hookhyper.ui.home

import android.annotation.SuppressLint
import android.content.Context
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
import com.newbieeming.hookhyper.core.common.UiStyle
import com.newbieeming.hookhyper.core.ui.feature.FeatureEntry
import com.newbieeming.hookhyper.core.ui.theme.LocalUiStyle
import com.newbieeming.hookhyper.ui.component.ScreenScaffold
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText

@Composable
fun HomeScreen(
    features: List<FeatureEntry>,
    onOpenFeature: (String) -> Unit,
) {
    val context = LocalContext.current
    val items = remember(context, features) {
        features.map { feature ->
            HomeFeature(feature, context.targetAppInfo(feature.targetPackageName))
        }.sortedBy { it.appInfo?.label ?: it.feature.targetPackageName }
    }
    ScreenScaffold(title = stringResource(R.string.app_name)) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.feature.targetPackageName }) { item ->
                FeatureRow(item, onClick = { onOpenFeature(item.feature.targetPackageName) })
            }
        }
    }
}

@Composable
private fun FeatureRow(item: HomeFeature, onClick: () -> Unit) {
    val feature = item.feature
    val appInfo = item.appInfo
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
                    MiuixText(appInfo?.label ?: feature.targetPackageName, fontWeight = FontWeight.SemiBold)
                    MiuixText(feature.targetPackageName)
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
                headlineContent = { Text(appInfo?.label ?: feature.targetPackageName, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(feature.targetPackageName, style = MaterialTheme.typography.labelMedium) },
                leadingContent = { FeatureIcon(appInfo) },
            )
        }
    }
}

@Composable
private fun FeatureIcon(appInfo: TargetAppInfo?) {
    if (appInfo != null) {
        Image(appInfo.icon, contentDescription = null, modifier = Modifier.size(48.dp))
    } else if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixIcon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(48.dp))
    } else {
        Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(48.dp))
    }
}

private data class HomeFeature(
    val feature: FeatureEntry,
    val appInfo: TargetAppInfo?,
)

private data class TargetAppInfo(
    val label: String,
    val icon: ImageBitmap,
)

@SuppressLint("QueryPermissionsNeeded")
private fun Context.targetAppInfo(packageName: String): TargetAppInfo? = runCatching {
    val info: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
    TargetAppInfo(
        label = packageManager.getApplicationLabel(info).toString(),
        icon = packageManager.getApplicationIcon(info).toBitmap().asImageBitmap(),
    )
}.getOrNull()
