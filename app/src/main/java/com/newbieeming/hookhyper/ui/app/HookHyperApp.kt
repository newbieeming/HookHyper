package com.newbieeming.hookhyper.ui.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
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
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.scene.rememberSceneState
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
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
            val onBack: () -> Unit = {
                backStack.removeLastOrNull()
                Unit
            }
            val entryDecorators = listOf<NavEntryDecorator<NavKey>>(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            )
            val entries = rememberDecoratedNavEntries(
                backStack = backStack,
                entryDecorators = entryDecorators,
                entryProvider = entryProvider<NavKey> {
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
                            onBack = onBack,
                            modifier = Modifier.fillMaxSize(),
                        ) ?: MissingFeatureScreen(onBack = onBack)
                    }
                },
            )
            val sceneState = rememberSceneState(
                entries = entries,
                sceneStrategies = listOf(SinglePaneSceneStrategy()),
                onBack = onBack,
            )
            val scene = sceneState.currentScene
            val navigationEventState = rememberNavigationEventState(
                currentInfo = SceneInfo(scene),
                backInfo = sceneState.previousScenes.map(::SceneInfo),
            )
            if (state.predictiveBackEnabled) {
                NavigationBackHandler(
                    state = navigationEventState,
                    isBackEnabled = scene.previousEntries.isNotEmpty(),
                    onBackCompleted = {
                        repeat(entries.size - scene.previousEntries.size) { onBack() }
                    },
                )
            } else {
                BackHandler(
                    enabled = scene.previousEntries.isNotEmpty(),
                    onBack = onBack,
                )
            }
            NavDisplay(
                sceneState = sceneState,
                navigationEventState = navigationEventState,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { it / 6 },
                        animationSpec = tween(320),
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it / 12 },
                        animationSpec = tween(320),
                    )
                },
                popTransitionSpec = {
                    EnterTransition.None togetherWith slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(320),
                    )
                },
                predictivePopTransitionSpec = { swipeEdge ->
                    val direction =
                        if (swipeEdge == NavigationEvent.EDGE_RIGHT) -1 else 1
                    EnterTransition.None togetherWith slideOutHorizontally(
                        targetOffsetX = { it * direction },
                    )
                },
            )
        }
    }
}
