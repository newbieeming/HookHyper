package com.newbieeming.hookhyper.ui.app

import android.os.Build
import android.view.RoundedCorner
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

/** At the end of a back swipe, keep 20% of the current page visible. */
internal const val PredictiveBackMaxTranslationFraction = 0.8f

/** Fallback gesture-zone width for devices that don't report system gesture insets. */
private val DefaultGestureZoneWidth = 20.dp

internal val LocalPredictiveBackProgress = staticCompositionLocalOf { 0f }
internal val LocalPredictiveBackShape = staticCompositionLocalOf<Shape> { RoundedCornerShape(28.dp) }
internal val LocalPredictiveBackGestureZoneWidth = staticCompositionLocalOf { DefaultGestureZoneWidth }

internal fun Modifier.predictiveBackClip(progress: Float, shape: Shape): Modifier = graphicsLayer {
    this.shape = shape
    clip = progress > 0f
}

/**
 * Reads the system gesture-zone width (left/right, whichever is larger)
 * from window insets.  Falls back to [DefaultGestureZoneWidth] on devices
 * that don't report gesture insets.
 */
@Composable
internal fun rememberGestureZoneWidth(): Dp {
    val view = LocalView.current
    val density = LocalDensity.current
    var gestureZoneWidthDp by remember { mutableStateOf(DefaultGestureZoneWidth) }

    LaunchedEffect(view) {
        view.doOnLayout {
            val insets = ViewCompat.getRootWindowInsets(view)
            val gestureInsets = insets?.getInsets(WindowInsetsCompat.Type.systemGestures())
            val fromInsets = gestureInsets?.let { max(it.left, it.right) } ?: 0
            gestureZoneWidthDp = if (fromInsets > 0) {
                with(density) { fromInsets.toDp() }
            } else {
                DefaultGestureZoneWidth
            }
        }
    }
    return gestureZoneWidthDp
}

@Composable
internal fun rememberPredictiveBackShape(): Shape {
    val view = LocalView.current
    val density = LocalDensity.current
    var cornerRadiusPx by remember { mutableIntStateOf(0) }

    LaunchedEffect(view) {
        view.doOnLayout {
            cornerRadiusPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(
                    RoundedCorner.POSITION_TOP_LEFT,
                    RoundedCorner.POSITION_TOP_RIGHT,
                    RoundedCorner.POSITION_BOTTOM_LEFT,
                    RoundedCorner.POSITION_BOTTOM_RIGHT,
                ).maxOf { position ->
                    view.rootWindowInsets?.getRoundedCorner(position)?.radius ?: 0
                }
            } else {
                0
            }
        }
    }
    val radius = with(density) { cornerRadiusPx.toDp() }.takeIf { it > 0.dp } ?: 28.dp
    return remember(radius) { RoundedCornerShape(radius) }
}
