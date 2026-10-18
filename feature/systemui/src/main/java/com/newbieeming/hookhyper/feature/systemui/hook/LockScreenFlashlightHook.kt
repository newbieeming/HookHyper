package com.newbieeming.hookhyper.feature.systemui.hook

import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.HookUtils.call
import com.newbieeming.hookhyper.core.hook.HookUtils.field
import com.newbieeming.hookhyper.core.hook.HookUtils.staticField
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.SettingsPreferenceGroup
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.component.FlashlightShortcut
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef
import java.lang.ref.WeakReference
import java.util.Collections
import java.util.WeakHashMap

@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class LockScreenFlashlightHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {
    override val def = SystemUiHookDef.LOCK_SCREEN_FLASHLIGHT_LONG_PRESS

    @Composable
    override fun Content() {
        SettingsPreferenceGroup {
            HookSwitchPreference(
                preferenceKey = preferenceKey,
                title = stringResource(R.string.systemui_flashlight_long_press_title),
                summary = stringResource(R.string.systemui_flashlight_long_press_summary),
            )
        }
    }

    override fun HookContext.onHook() {
        val hookedLoaders = Collections.newSetFromMap(WeakHashMap<ClassLoader, Boolean>())
        val shortcuts = ShortcutBindings(classLoader)

        hookSafely("Flashlight shortcut plugin") {
            val method = "com.android.keyguard.shortcut.MiuiShortcutController".toClass().resolve()
                .firstMethod {
                    name = "onPluginLoaded"
                    parameterCount = 3
                }
                .self
            xposed.hook(method).intercept { chain ->
                val plugin = chain.args[0]
                val loader = plugin?.javaClass?.classLoader
                if (loader != null && hookedLoaders.add(loader)) {
                    hookShortcutGestures(loader, shortcuts)
                }
                val result = chain.proceed()
                runCatching { plugin?.field("shortcutViewLayoutController") }.getOrNull()?.let(shortcuts::bind)
                result
            }
        }
    }

    private fun HookContext.hookShortcutGestures(loader: ClassLoader, shortcuts: ShortcutBindings) {
        hookSafely("Flashlight shortcut gestures") {
            val layoutClass = loader.loadClass("com.miui.keyguard.shortcuts.controller.ShortcutViewLayoutController")
            listOf("updateShortcutView", "updateShortcutViewImageTintList", "updateShortcutFixedSize").forEach { name ->
                val updateMethod = layoutClass.declaredMethods.first { it.name == name }
                xposed.hook(updateMethod).intercept { update ->
                    val result = update.proceed()
                    update.thisObject?.let(shortcuts::bind)
                    result
                }
            }
            xposed.hook(layoutClass.getDeclaredMethod("release")).intercept { release ->
                release.thisObject?.let(shortcuts::release)
                release.proceed()
            }
            xposed.hook(layoutClass.getDeclaredMethod("handleOnIconClick", Boolean::class.javaPrimitiveType)).intercept { click ->
                if (shortcuts.onIconClick(click.thisObject, click.args[0] == true)) null else click.proceed()
            }
            val moveClass = loader.loadClass("com.miui.keyguard.shortcuts.controller.ShortcutMoveController")
            xposed.hook(moveClass.getDeclaredMethod("onTouchEvent", MotionEvent::class.java)).intercept { touch ->
                shortcuts.onTouch(requireNotNull(touch.thisObject), touch.args[0] as MotionEvent) ?: touch.proceed()
            }
            xposed.hook(moveClass.getDeclaredMethod("destroy")).intercept { destroy ->
                shortcuts.cancelGesture(destroy.thisObject)
                destroy.proceed()
            }
        }
    }

    private class ShortcutBindings(private val classLoader: ClassLoader) {
        private val bindings = WeakHashMap<ImageView, WeakReference<FlashlightShortcut>>()
        private val gestures = WeakHashMap<Any, FlashlightShortcut>()
        private val listenerClass = classLoader.loadClass("com.android.systemui.statusbar.policy.FlashlightController\$FlashlightListener")

        fun bind(layoutController: Any) {
            runCatching {
                val loader = layoutController.javaClass.classLoader!!
                val manager = requireNotNull(loader.loadClass("com.miui.keyguard.shortcuts.manager.MiuiShortcutManager").staticField("INSTANCE"))
                val flashlight = FlashlightShortcut.controller(classLoader)
                listOf(LEFT_VIEW, RIGHT_VIEW).forEachIndexed { index, name ->
                    val icon = layoutController.field(name) as ImageView
                    val binding = bindings[icon]?.get() ?: FlashlightShortcut(
                        icon,
                        manager,
                        index == 0,
                        flashlight,
                        listenerClass,
                    ).also { bindings[icon] = WeakReference(it) }
                    binding.refresh()
                }
            }.onFailure { Log.e(TAG, "Unable to bind flashlight shortcuts", it) }
        }

        fun release(controller: Any) {
            listOf(LEFT_VIEW, RIGHT_VIEW).forEach { name ->
                bindings.remove(controller.field(name) as ImageView)?.get()?.release()
            }
        }

        fun onIconClick(controller: Any?, left: Boolean): Boolean {
            val icon = controller?.field(if (left) LEFT_VIEW else RIGHT_VIEW) as? ImageView
            val binding = bindings[icon]?.get()
            if (binding?.isFlashlight() != true) return false
            binding.onAccessibilityClick()
            return true
        }

        fun onTouch(controller: Any, event: MotionEvent): Boolean? {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                cancelGesture(controller)
                if (controller.call("interceptTouchEvent") != true) {
                    findTouchedShortcut(controller, event)?.let { gestures[controller] = it }
                }
            }
            val binding = gestures[controller] ?: return null
            binding.onTouch(event)
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                gestures.remove(controller)
            }
            return true
        }

        private fun findTouchedShortcut(controller: Any, event: MotionEvent): FlashlightShortcut? = listOf("rightIcon", "leftIcon").firstNotNullOfOrNull { name ->
            val icon = controller.field(name) as? ImageView
            bindings[icon]?.get()?.takeIf {
                it.isFlashlight() && controller.call("isTouchOnIcon", icon, event.x, event.y) == true
            }
        }

        fun cancelGesture(controller: Any?) {
            gestures.remove(controller)?.cancelGesture()
        }
    }

    private companion object {
        const val LEFT_VIEW = "shortcutViewLeft"
        const val RIGHT_VIEW = "shortcutViewRight"
        const val TAG = "LockScreenFlashlight"
    }
}
