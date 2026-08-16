package com.newbieeming.hookhyper.ui.app

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.newbieeming.hookhyper.core.ui.theme.HookHyperTheme
import com.newbieeming.hookhyper.ui.component.AppScaffold
import com.newbieeming.hookhyper.ui.component.AppTab
import com.newbieeming.hookhyper.ui.feature.MissingFeatureScreen
import com.newbieeming.hookhyper.ui.home.HomeScreen
import com.newbieeming.hookhyper.ui.navigation.FeatureRoute
import com.newbieeming.hookhyper.ui.navigation.HomeRoute
import com.newbieeming.hookhyper.ui.navigation.SettingsRoute
import com.newbieeming.hookhyper.ui.settings.SettingsScreen

@Composable
fun HookHyperApp(viewModel: AppViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(HomeRoute)

    HookHyperTheme(style = state.uiStyle) {
        val currentRoute = backStack.lastOrNull()
        AppScaffold(
            selectedTab = if (currentRoute is SettingsRoute) AppTab.SETTINGS else AppTab.HOME,
            onHome = {
                backStack.clear()
                backStack.add(HomeRoute)
            },
            onSettings = {
                backStack.clear()
                backStack.add(SettingsRoute)
            },
        ) { padding ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<HomeRoute> {
                        HomeScreen(
                            features = state.features,
                            onOpenFeature = { backStack.add(FeatureRoute(it)) },
                        )
                    }
                    entry<SettingsRoute> {
                        SettingsScreen(
                            state = state,
                            onIntent = viewModel::accept,
                        )
                    }
                    entry<FeatureRoute> { route ->
                        viewModel.feature(route.featureId)?.Content(
                            onBack = { backStack.removeLastOrNull() },
                            modifier = Modifier.fillMaxSize(),
                        ) ?: MissingFeatureScreen(onBack = { backStack.removeLastOrNull() })
                    }
                },
            )
        }
    }
}
