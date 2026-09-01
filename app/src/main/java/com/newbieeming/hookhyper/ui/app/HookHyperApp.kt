package com.newbieeming.hookhyper.ui.app

import android.os.Build
import android.view.RoundedCorner
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.doOnLayout
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
import androidx.navigation3.scene.SceneState
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.scene.rememberSceneState
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.NavigationEventState
import androidx.navigationevent.compose.rememberNavigationEventState
import com.newbieeming.hookhyper.core.ui.theme.HookHyperTheme
import com.newbieeming.hookhyper.ui.feature.MissingFeatureScreen
import com.newbieeming.hookhyper.ui.navigation.FeatureRoute
import com.newbieeming.hookhyper.ui.navigation.HomeRoute
import com.newbieeming.hookhyper.ui.navigation.HookCategoryRoute

@Composable
private fun rememberDeviceCornerRadius(): RoundedCornerShape {
    val view = LocalView.current
    val density = LocalDensity.current
    var radiusPx by remember { mutableIntStateOf(0) }
    LaunchedEffect(view) {
        view.doOnLayout {
            radiusPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(
                    RoundedCorner.POSITION_TOP_LEFT,
                    RoundedCorner.POSITION_TOP_RIGHT,
                    RoundedCorner.POSITION_BOTTOM_LEFT,
                    RoundedCorner.POSITION_BOTTOM_RIGHT,
                ).maxOf { view.rootWindowInsets?.getRoundedCorner(it)?.radius ?: 0 }
            } else {
                0
            }
        }
    }
    val radius = with(density) { radiusPx.toDp() }.takeIf { it > 0.dp }?.minus(2.dp)?.coerceAtLeast(0.dp) ?: 0.dp
    return remember(radius) { RoundedCornerShape(radius) }
}

private val TransitionEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
private const val SlideDuration = 500
private const val FadeDuration = 300
private const val FadeDelay = 100
private const val PredictiveDuration = 400
private const val SlideOffsetDivisor = 4

@Composable
fun HookHyperApp(viewModel: AppViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(HomeRoute)
    val cornerRadius = rememberDeviceCornerRadius()

    HookHyperTheme {
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
                        features = viewModel.features,
                        onIntent = viewModel::accept,
                        onOpenFeature = { backStack.add(FeatureRoute(it)) },
                    )
                }
                entry<FeatureRoute> { route ->
                    viewModel.feature(route.targetPackageName)?.Content(
                        onBack = onBack,
                        onOpenCategory = { categoryId ->
                            backStack.add(HookCategoryRoute(route.targetPackageName, categoryId))
                        },
                        categoryId = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                shape = cornerRadius
                                clip = true
                            },
                    ) ?: MissingFeatureScreen(onBack = onBack)
                }
                entry<HookCategoryRoute> { route ->
                    viewModel.feature(route.targetPackageName)?.Content(
                        onBack = onBack,
                        onOpenCategory = {},
                        categoryId = route.categoryId,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                shape = cornerRadius
                                clip = true
                            },
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

        HookHyperNavDisplay(
            sceneState = sceneState,
            navigationEventState = navigationEventState,
        )
    }
}

@Composable
private fun HookHyperNavDisplay(
    sceneState: SceneState<NavKey>,
    navigationEventState: NavigationEventState<SceneInfo<NavKey>>,
) {
    NavDisplay(
        sceneState = sceneState,
        navigationEventState = navigationEventState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(SlideDuration, easing = TransitionEasing),
            ) { it } +
                fadeIn(animationSpec = tween(FadeDuration, FadeDelay, easing = TransitionEasing)) togetherWith
                slideOutHorizontally(
                    animationSpec = tween(SlideDuration, easing = TransitionEasing),
                ) { -it / SlideOffsetDivisor } +
                fadeOut(animationSpec = tween(FadeDuration, easing = TransitionEasing))
        },
        popTransitionSpec = {
            slideInHorizontally(
                animationSpec = tween(SlideDuration, easing = TransitionEasing),
            ) { -it / SlideOffsetDivisor } +
                fadeIn(animationSpec = tween(FadeDuration, FadeDelay, easing = TransitionEasing)) togetherWith
                slideOutHorizontally(
                    animationSpec = tween(SlideDuration, easing = TransitionEasing),
                ) { it } +
                fadeOut(animationSpec = tween(FadeDuration, easing = TransitionEasing))
        },
        predictivePopTransitionSpec = {
            fadeIn(
                animationSpec = tween(PredictiveDuration, easing = TransitionEasing),
                initialAlpha = 0.1f,
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(PredictiveDuration, easing = TransitionEasing),
            ) { width -> width }
        },
    )
}
