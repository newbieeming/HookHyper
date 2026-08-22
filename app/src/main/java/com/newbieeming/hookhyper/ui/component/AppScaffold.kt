package com.newbieeming.hookhyper.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.newbieeming.hookhyper.R
import com.newbieeming.hookhyper.core.common.UiStyle
import com.newbieeming.hookhyper.core.ui.theme.LocalUiStyle
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold

enum class AppTab {
    HOME,
    SETTINGS,
}

@Composable
fun AppScaffold(
    selectedTab: AppTab,
    onHome: () -> Unit,
    onSettings: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val homeLabel = stringResource(R.string.nav_home)
    val settingsLabel = stringResource(R.string.nav_settings)
    if (LocalUiStyle.current == UiStyle.MIUIX) {
        MiuixScaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                MiuixNavigationBar {
                    MiuixNavigationBarItem(
                        selected = selectedTab == AppTab.HOME,
                        onClick = onHome,
                        icon = Icons.Default.Home,
                        label = homeLabel,
                    )
                    MiuixNavigationBarItem(
                        selected = selectedTab == AppTab.SETTINGS,
                        onClick = onSettings,
                        icon = Icons.Default.Settings,
                        label = settingsLabel,
                    )
                }
            },
            content = content,
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                MaterialNavigationItems(
                    selectedTab = selectedTab,
                    onHome = onHome,
                    onSettings = onSettings,
                )
            },
            content = content,
        )
    }
}

@Composable
private fun MaterialNavigationItems(
    selectedTab: AppTab,
    onHome: () -> Unit,
    onSettings: () -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == AppTab.HOME,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.SETTINGS,
            onClick = onSettings,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_settings)) },
        )
    }
}
