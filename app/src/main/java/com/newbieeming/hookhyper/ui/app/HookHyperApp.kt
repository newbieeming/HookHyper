package com.newbieeming.hookhyper.ui.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.scene.rememberSceneState
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.newbieeming.hookhyper.core.ui.theme.HookHyperTheme
import com.newbieeming.hookhyper.ui.feature.MissingFeatureScreen
import com.newbieeming.hookhyper.ui.navigation.FeatureRoute
import com.newbieeming.hookhyper.ui.navigation.HomeRoute

/** Root navigation and detail-page predictive-back integration. */
@Composable
fun HookHyperApp(viewModel: AppViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(HomeRoute)
    val predictiveBackShape = rememberPredictiveBackShape()
    val gestureZoneWidth = rememberGestureZoneWidth()

    HookHyperTheme(style = state.uiStyle) {
        val onBack: () -> Unit = { backStack.removeLastOrNull() }
        val entryDecorators = listOf<NavEntryDecorator<NavKey>>(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        )
        val entries = rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = entryDecorators,
            entryProvider = entryProvider {
                entry<HomeRoute> {
                    MainTabs(
                        state = state,
                        onIntent = viewModel::accept,
                        onOpenFeature = { backStack.add(FeatureRoute(it)) },
                    )
                }
                entry<FeatureRoute> { route ->
                    viewModel.feature(route.featureId)?.Content(
                        onBack = onBack,
                        modifier = Modifier
                            .fillMaxSize()
                            .predictiveBackClip(
                                progress = LocalPredictiveBackProgress.current,
                                shape = LocalPredictiveBackShape.current,
                            ),
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

        val navigationProgress =
            (navigationEventState.transitionState as? NavigationEventTransitionState.InProgress)
                ?.latestEvent
                ?.progress
                ?.coerceIn(0f, 1f)
                ?: 0f
        CompositionLocalProvider(
            LocalPredictiveBackProgress provides navigationProgress,
            LocalPredictiveBackShape provides predictiveBackShape,
            LocalPredictiveBackGestureZoneWidth provides gestureZoneWidth,
        ) {
            NavDisplay(
                sceneState = sceneState,
                navigationEventState = navigationEventState,
                modifier = Modifier.fillMaxSize(),
                predictivePopTransitionSpec = { swipeEdge ->
                    val direction = if (swipeEdge == NavigationEvent.EDGE_RIGHT) -1 else 1
                    EnterTransition.None togetherWith slideOutHorizontally { width ->
                        (width * PredictiveBackMaxTranslationFraction * direction).toInt()
                    }
                },
            )
        }
    }
}
