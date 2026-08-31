package com.newbieeming.hookhyper.ui.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.newbieeming.hookhyper.core.ui.feature.FeatureEntry
import com.newbieeming.hookhyper.ui.component.AppScaffold
import com.newbieeming.hookhyper.ui.component.AppTab
import com.newbieeming.hookhyper.ui.home.HomeScreen
import com.newbieeming.hookhyper.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

private val PagerNavigationSpringSpec: SpringSpec<Float> = spring(
    stiffness = 322.2f,
    dampingRatio = 32.31f / (2f * kotlin.math.sqrt(322.2f)),
    visibilityThreshold = 0.5f,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MainTabs(
    state: AppState,
    features: List<FeatureEntry>,
    onIntent: (AppIntent) -> Unit,
    onOpenFeature: (String) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { AppTab.entries.size })
    val scope = rememberCoroutineScope()
    val selectedTab = AppTab.entries[pagerState.settledPage]
    val selectTab: (AppTab) -> Unit = { tab ->
        if (pagerState.currentPage != tab.ordinal) {
            scope.launch { pagerState.animateToTab(tab.ordinal) }
        }
    }
    val isPagerBackEnabled = pagerState.settledPage != AppTab.HOME.ordinal

    BackHandler(
        enabled = isPagerBackEnabled,
        onBack = { selectTab(AppTab.HOME) },
    )

    AppScaffold(
        selectedTab = selectedTab,
        onHome = { selectTab(AppTab.HOME) },
        onSettings = { selectTab(AppTab.SETTINGS) },
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = AppTab.entries.size - 1,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
        ) { page ->
            when (AppTab.entries[page]) {
                AppTab.HOME -> HomeScreen(
                    features = features,
                    onOpenFeature = onOpenFeature,
                )
                AppTab.SETTINGS -> SettingsScreen(
                    state = state,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private suspend fun PagerState.animateToTab(page: Int) {
    animateScrollToPage(page = page, animationSpec = PagerNavigationSpringSpec)
}
