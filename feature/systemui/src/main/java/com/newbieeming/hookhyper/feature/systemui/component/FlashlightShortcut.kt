package com.newbieeming.hookhyper.feature.systemui.component

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import com.newbieeming.hookhyper.core.hook.HookUtils.call
import com.newbieeming.hookhyper.core.hook.HookUtils.field
import com.newbieeming.hookhyper.core.hook.HookUtils.staticField
import java.lang.reflect.Proxy

/** Uses the same controller and colors as the Control Center flashlight tile. */
internal class FlashlightShortcut(
    val icon: ImageView,
    private val manager: Any,
    private val left: Boolean,
    private val controller: Any,
    listenerClass: Class<*>,
) : View.OnAttachStateChangeListener {
    private val handler = Handler(Looper.getMainLooper())
    private val touchSlop = ViewConfiguration.get(icon.context).scaledTouchSlop.toFloat()
    private var tracking = false
    private var downX = 0f
    private var downY = 0f
    private var registered = false
    private var originalTint: ColorStateList? = null
    private var selected = false

    // The system's Folme touch handler also owns the icon's scale. Animate the shared
    // container so the icon and glass respond together without competing for that property.
    private val pressTarget = icon.parent as? View ?: icon
    private var pressAnimator: ValueAnimator? = null
    private var pressVisualActive = false
    private var restingScaleX = 1f
    private var restingScaleY = 1f
    private val tileContext = icon.context.createPackageContext("miui.systemui.plugin", 0)
    private val selectedColor = tileColor("qs_enabled_color")
    private val selectedIconColor = tileColor("qs_miui_flashlight_icon_enabled_color")
    private val listener = Proxy.newProxyInstance(listenerClass.classLoader, arrayOf(listenerClass)) { proxy, method, args ->
        when (method.name) {
            "hashCode" -> System.identityHashCode(proxy)
            "equals" -> proxy === args?.getOrNull(0)
            "toString" -> "HookHyperFlashlightListener"
            else -> {
                handler.post { if (registered) refresh() }
                null
            }
        }
    }
    private val longPress = Runnable {
        if (tracking && icon.isShown && toggle()) animatePress(false)
    }

    init {
        icon.addOnAttachStateChangeListener(this)
        if (icon.isAttachedToWindow) onViewAttachedToWindow(icon)
    }

    fun isFlashlight(): Boolean = runCatching {
        val data = manager.call(if (left) "getShortcutDataLeft" else "getShortcutDataRight")
        data?.call("getPackageName") == "miui.systemui.plugin" && data.call("getTag") == "FLASHLIGHT_SET"
    }.getOrDefault(false)

    fun onAccessibilityClick() {
        if (manager.call("getTalkbackEnabled") == true) toggle()
    }

    private fun toggle(): Boolean = runCatching {
        val enabled = controller.call("isEnabled") == true
        if (isFlashlight() && (enabled || controller.call("isAvailable") == true)) {
            controller.call("setFlashlight", !enabled)
            icon.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            true
        } else {
            false
        }
    }.onFailure { Log.e(TAG, "Unable to toggle flashlight", it) }.getOrDefault(false)

    fun onTouch(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            cancelGesture()
            tracking = true
            downX = event.x
            downY = event.y
            animatePress(true)
            handler.postDelayed(longPress, ViewConfiguration.getLongPressTimeout().toLong())
        } else if (tracking) {
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> if (
                    kotlin.math.hypot(event.x - downX, event.y - downY) > touchSlop
                ) {
                    cancelGesture()
                }
                MotionEvent.ACTION_POINTER_DOWN -> cancelGesture()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> cancelGesture()
            }
        }
        return true
    }

    fun cancelGesture() {
        handler.removeCallbacks(longPress)
        tracking = false
        animatePress(false)
    }

    private fun animatePress(pressed: Boolean) {
        if (!pressed && !pressVisualActive) return
        if (pressed && !pressVisualActive) {
            restingScaleX = pressTarget.scaleX
            restingScaleY = pressTarget.scaleY
            pressVisualActive = true
        }
        stopPressAnimator()
        val startX = pressTarget.scaleX
        val startY = pressTarget.scaleY
        val targetX = restingScaleX * if (pressed) PRESSED_SCALE else 1f
        val targetY = restingScaleY * if (pressed) PRESSED_SCALE else 1f
        pressAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = if (pressed) PRESS_DURATION else RELEASE_DURATION
            interpolator = if (pressed) DecelerateInterpolator() else OvershootInterpolator(RELEASE_TENSION)
            addUpdateListener {
                val progress = it.animatedValue as Float
                pressTarget.scaleX = startX + (targetX - startX) * progress
                pressTarget.scaleY = startY + (targetY - startY) * progress
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    finishPressAnimation(animation, pressed)
                }
            })
        }
        pressAnimator?.start()
    }

    private fun finishPressAnimation(animation: Animator, pressed: Boolean) {
        if (pressAnimator !== animation) return
        pressAnimator = null
        if (!pressed) {
            pressTarget.scaleX = restingScaleX
            pressTarget.scaleY = restingScaleY
            pressVisualActive = false
        }
    }

    private fun stopPressAnimator() {
        val previous = pressAnimator
        pressAnimator = null
        previous?.cancel()
    }

    fun refresh() {
        runCatching { updateState() }
            .onFailure { Log.e(TAG, "Unable to update flashlight shortcut state", it) }
    }

    private fun updateState() {
        val active = isFlashlight() && controller.call("isEnabled") == true
        if (active) {
            // A theme update may replace the tint while the flashlight is enabled.
            if (!selected || icon.imageTintList?.defaultColor != selectedIconColor) originalTint = icon.imageTintList
            icon.imageTintList = ColorStateList.valueOf(selectedIconColor)
        } else if (selected && icon.imageTintList?.defaultColor == selectedIconColor) {
            icon.imageTintList = originalTint
        }
        selected = active
        icon.isActivated = active
        ShortcutGlassBackground.setSelectedState(icon, active, selectedColor)
    }

    override fun onViewAttachedToWindow(view: View) {
        if (registered) return
        registered = true
        runCatching { controller.call("addCallback", listener) }
            .onFailure {
                registered = false
                Log.e(TAG, "Unable to observe flashlight", it)
            }
        refresh()
    }

    override fun onViewDetachedFromWindow(view: View) {
        cancelGesture()
        stopPressAnimator()
        if (pressVisualActive) {
            pressTarget.scaleX = restingScaleX
            pressTarget.scaleY = restingScaleY
            pressVisualActive = false
        }
        if (registered) {
            registered = false
            runCatching { controller.call("removeCallback", listener) }
                .onFailure { Log.w(TAG, "Unable to remove flashlight listener", it) }
        }
        handler.removeCallbacksAndMessages(null)
    }

    fun release() {
        onViewDetachedFromWindow(icon)
        icon.removeOnAttachStateChangeListener(this)
        if (selected) icon.imageTintList = originalTint
        icon.isActivated = false
        ShortcutGlassBackground.setSelectedState(icon, false, selectedColor)
    }

    private fun tileColor(name: String): Int {
        val id = tileContext.resources.getIdentifier(name, "color", tileContext.packageName)
        check(id != 0) { "Missing Control Center color: $name" }
        return tileContext.getColor(id)
    }

    companion object {
        private const val TAG = "FlashlightShortcut"
        private const val PRESSED_SCALE = 0.9f
        private const val PRESS_DURATION = 120L
        private const val RELEASE_DURATION = 220L
        private const val RELEASE_TENSION = 1.2f

        fun controller(loader: ClassLoader): Any {
            val stub = requireNotNull(loader.loadClass("miui.stub.MiuiStub").staticField("INSTANCE"))
            val provider = requireNotNull(stub.field("mSysUIProvider"))
            return requireNotNull(provider.field("mFlashlightController")?.call("get"))
        }
    }
}
