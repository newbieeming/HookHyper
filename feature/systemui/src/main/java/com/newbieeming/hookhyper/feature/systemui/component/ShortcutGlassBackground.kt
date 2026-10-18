package com.newbieeming.hookhyper.feature.systemui.component

import android.content.Context
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import com.newbieeming.hookhyper.feature.systemui.model.LockScreenShortcutGlassSettings
import kotlin.math.roundToInt

/** A separate material surface keeps the shortcut's drawable and touch target unchanged. */
internal class ShortcutGlassBackground private constructor(
    context: Context,
    private val icon: ImageView,
    settings: LockScreenShortcutGlassSettings,
    private val material: ShortcutGlassMaterial,
) : ImageView(context),
    ViewTreeObserver.OnPreDrawListener {
    private val radiusPx = settings.cornerRadiusDp * resources.displayMetrics.density
    private var observer: ViewTreeObserver? = null
    private var materialReady = false
    private val selectedBackground = GradientDrawable().apply { cornerRadius = radiusPx }
    private val shortcutOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radiusPx)
        }
    }

    init {
        tag = TAG
        isClickable = false
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        layoutParams = FrameLayout.LayoutParams(
            (settings.widthDp * resources.displayMetrics.density).roundToInt(),
            (settings.heightDp * resources.displayMetrics.density).roundToInt(),
        )
        outlineProvider = shortcutOutlineProvider
        clipToOutline = true
        visibility = INVISIBLE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        runCatching {
            material.apply(this, context)
            material.configureClipping(this)
            if (isActivated) background = selectedBackground
            if (width > 0 && height > 0) material.updateSize(this)
            materialReady = true
        }.onFailure { Log.e(TAG, "Unable to initialize native glass material", it) }
        observer = icon.viewTreeObserver.apply { addOnPreDrawListener(this@ShortcutGlassBackground) }
    }

    override fun onDetachedFromWindow() {
        observer?.takeIf { it.isAlive }?.removeOnPreDrawListener(this)
        observer = null
        runCatching { material.clear(this) }
            .onFailure { Log.w(TAG, "Unable to clear native glass material", it) }
        materialReady = false
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        invalidateOutline()
        if (materialReady && width > 0 && height > 0) {
            runCatching { material.updateSize(this) }
                .onFailure { Log.e(TAG, "Unable to resize native glass material", it) }
        }
    }

    override fun onPreDraw(): Boolean {
        visibility = if (materialReady && icon.visibility == VISIBLE && icon.drawable != null) VISIBLE else INVISIBLE
        // Mirror transforms without changing the icon layout, click/long-press handlers or tint.
        val centeredX = icon.x + (icon.width - width) / 2f
        val centeredY = icon.y + (icon.height - height) / 2f
        // An odd-sized touch target around an even-sized surface produces half-pixel bounds.
        // Align the resting glass to pixels, but retain continuous coordinates during gestures.
        val atRest = icon.scaleX == 1f && icon.scaleY == 1f && icon.rotation == 0f &&
            icon.translationX == 0f && icon.translationY == 0f
        x = if (atRest) centeredX.roundToInt().toFloat() else centeredX
        y = if (atRest) centeredY.roundToInt().toFloat() else centeredY
        pivotX = width / 2f + icon.pivotX - icon.width / 2f
        pivotY = height / 2f + icon.pivotY - icon.height / 2f
        scaleX = icon.scaleX
        scaleY = icon.scaleY
        rotation = icon.rotation
        alpha = icon.alpha
        return true
    }

    companion object {
        private const val TAG = "HookHyperShortcutGlass"

        fun setSelectedState(icon: ImageView, selected: Boolean, color: Int) {
            val surface = (icon.parent as? FrameLayout)?.findViewWithTag<ShortcutGlassBackground>(TAG) ?: return
            if (surface.isActivated == selected) return
            surface.isActivated = selected
            surface.selectedBackground.setColor(color)
            surface.background = if (selected) surface.selectedBackground else null
        }

        fun install(
            icon: ImageView,
            context: Context,
            settings: LockScreenShortcutGlassSettings,
            material: ShortcutGlassMaterial,
        ) {
            val parent = icon.parent as? FrameLayout ?: return
            if (parent.findViewWithTag<View>(TAG) != null) return
            val background = ShortcutGlassBackground(context, icon, settings, material)
            parent.addView(background, parent.indexOfChild(icon))
        }
    }
}
