package com.newbieeming.hookhyper.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.core.common.UiStyle
import com.newbieeming.hookhyper.core.ui.component.SystemSettingsTopBar
import com.newbieeming.hookhyper.core.ui.theme.LocalUiStyle
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.Text as MiuixText

@Composable
fun ScreenScaffold(
    title: String,
    content: @Composable (PaddingValues) -> Unit,
) {
    if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixScaffold(
            topBar = { SystemSettingsTopBar(title = title) },
            content = content,
        )
    } else {
        Scaffold(
            topBar = { SystemSettingsTopBar(title = title) },
            content = content,
        )
    }
}

@Composable
fun AdaptiveCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixCard(
            modifier = modifier,
            insideMargin = PaddingValues(18.dp),
            content = content,
        )
    } else {
        Card(modifier) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content,
            )
        }
    }
}

@Composable
fun AdaptiveText(text: String, emphasized: Boolean = false) {
    if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixText(text, fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal)
    } else {
        Text(
            text,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
        )
    }
}
