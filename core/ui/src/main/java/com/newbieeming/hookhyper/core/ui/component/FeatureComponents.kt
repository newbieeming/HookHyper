package com.newbieeming.hookhyper.core.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureScaffold(
    title: String,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    isRestarting: Boolean,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SystemSettingsTopBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRestart, enabled = !isRestarting) {
                        RestartActionContent(isRestarting)
                    }
                },
            )
        },
    ) { padding ->
        FeatureContent(padding, content)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LazyFeatureScaffold(
    title: String,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    isRestarting: Boolean,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SystemSettingsTopBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRestart, enabled = !isRestarting) {
                        RestartActionContent(isRestarting)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemSettingsTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    LargeTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Normal) },
        modifier = modifier,
        navigationIcon = {
            navigationIcon?.invoke()
        },
        actions = actions,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
        scrollBehavior = scrollBehavior,
    )
}

/** Shared corners for connected settings rows and standalone cards. */
fun settingsItemShape(index: Int = 0, count: Int = 1): RoundedCornerShape = RoundedCornerShape(
    topStart = if (index == 0) 24.dp else 4.dp,
    topEnd = if (index == 0) 24.dp else 4.dp,
    bottomStart = if (index == count - 1) 24.dp else 4.dp,
    bottomEnd = if (index == count - 1) 24.dp else 4.dp,
)

internal val LocalSettingsPreferenceGroup = staticCompositionLocalOf { false }

/** Keeps a feature switch and its expanding options in one connected group. */
@Composable
fun SettingsPreferenceGroup(
    index: Int = 0,
    count: Int = 1,
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalSettingsPreferenceGroup provides true) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = if (count > 1) 2.dp else 6.dp)
                .fillMaxWidth()
                .clip(settingsItemShape(index, count)),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content,
        )
    }
}

@Composable
private fun RestartActionContent(isRestarting: Boolean) {
    if (isRestarting) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
        )
    } else {
        Icon(
            Icons.Default.RestartAlt,
            contentDescription = stringResource(R.string.action_restart_app),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeatureContent(
    padding: PaddingValues,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBringIntoViewSpec provides CenteredBringIntoViewSpec) {
        val density = LocalDensity.current
        val isImeVisible = WindowInsets.ime.getBottom(density) > 0
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .fillMaxSize(),
        ) {
            val focusedFieldTrailingSpace = maxHeight / 2
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                content()
                if (isImeVisible) {
                    Spacer(Modifier.height(focusedFieldTrailingSpace))
                }
            }
        }
    }
}

@Composable
fun SwitchPreference(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grouped = LocalSettingsPreferenceGroup.current
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .padding(horizontal = if (grouped) 0.dp else 16.dp, vertical = if (grouped) 0.dp else 6.dp)
            .fillMaxWidth(),
        shape = if (grouped) RoundedCornerShape(4.dp) else settingsItemShape(),
        color = colors.surfaceContainerLow,
    ) {
        ListItem(
            modifier = Modifier
                .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(vertical = 8.dp),
            headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(summary) },
            trailingContent = {
                Switch(checked = checked, onCheckedChange = null)
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
                headlineColor = colors.onSurface,
                supportingColor = colors.onSurfaceVariant,
            ),
        )
    }
}

/** 打开 Hook 分类二级页的列表项。 */
@Composable
fun HookCategoryPreference(
    category: HookCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int = 0,
    count: Int = 1,
) {
    val presentation = category.presentation()
    val title = stringResource(category.titleResId)
    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .fillMaxWidth()
            .clip(settingsItemShape(index, count))
            .clickable(onClick = onClick),
        shape = settingsItemShape(index, count),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(presentation)
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun CategoryIcon(presentation: CategoryPresentation) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(presentation.background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = presentation.imageVector,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = presentation.foreground,
        )
    }
}

private data class CategoryPresentation(
    val imageVector: ImageVector,
    val background: Color,
    val foreground: Color,
)

@Composable
private fun HookCategory.presentation(): CategoryPresentation {
    val colors = MaterialTheme.colorScheme
    return when (id) {
        "lock_screen" -> CategoryPresentation(Icons.Default.Lock, colors.primaryContainer, colors.onPrimaryContainer)
        "status_bar" -> CategoryPresentation(Icons.Default.SignalCellularAlt, colors.secondaryContainer, colors.onSecondaryContainer)
        "notification_bar" -> CategoryPresentation(Icons.Default.Notifications, colors.tertiaryContainer, colors.onTertiaryContainer)
        "super_island" -> CategoryPresentation(Icons.Default.ViewCarousel, colors.primaryContainer, colors.onPrimaryContainer)
        "device" -> CategoryPresentation(Icons.Default.PhoneAndroid, colors.secondaryContainer, colors.onSecondaryContainer)
        else -> CategoryPresentation(Icons.Default.Settings, colors.secondaryContainer, colors.onSecondaryContainer)
    }
}

private object CenteredBringIntoViewSpec : BringIntoViewSpec {
    override fun calculateScrollDistance(
        offset: Float,
        size: Float,
        containerSize: Float,
    ): Float = offset - (containerSize - size) / 2f
}
