package com.newbieeming.hookhyper.feature.systemui.hook

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef

@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class SoftLightGlassHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {

    override val def = SystemUiHookDef.FORCE_SOFT_LIGHT_GLASS

    private companion object {
        private const val TAG = "SoftLightGlassHook"
    }

    @Composable
    override fun Content() {
        SettingsPreferenceGroup {
            HookSwitchPreference(
                preferenceKey = preferenceKey,
                title = stringResource(R.string.systemui_soft_light_glass_title),
                summary = stringResource(R.string.systemui_soft_light_glass_summary),
            )
        }
    }

    override fun HookContext.onHook() {
        val materialType = "com.miui.systemui.material.MaterialType".toClass()
        val blur = materialType.staticField("BLUR")
        val glass = materialType.staticField("GLASS")

        hookInvokeSuspend("NotificationMaterialStateInteractor",
            "com.android.systemui.statusbar.notification.style.domain.NotificationMaterialStateInteractor\$materialTypeState\$1"
        ) {
            _, original ->
            if (original == blur) glass else original
        }

        val bionics = runCatching {
            "com.miui.interfaces.controlcenter.data.repository.MaterialMode\$Bionics"
                .toClass().staticField("INSTANCE")
        }.getOrNull()

        // 260916前 / 260916后
        listOf(
            "ShadeBlendBlurController" to "com.miui.systemui.shade.blur.ShadeBlendBlurController\$isBionicsEnabled\$1",
            "ShadeBlendBlurControllerImpl" to "com.miui.systemui.shade.blur.ShadeBlendBlurControllerImpl\$isBionicsEnabled\$1",
        ).forEach { (name, className) ->
            hookInvokeSuspend(name, className) {
                instance, original ->
                if (bionics != null && instance.field("L\$0") == bionics) true else original
            }
        }

        var pluginHooked = false
        hookSafely("ControlCenterContentController") {
            val method = "com.miui.systemui.controlcenter.container.ControlCenterContentController".toClass()
                .resolve()
                .firstMethod {
                    name = "onPluginLoaded"
                    parameterCount = 3
                }.self
            xposed.hook(method).intercept { chain ->
                val original = chain.proceed()
                if (!pluginHooked) {
                    val pluginLoader = chain.args[0]?.javaClass?.classLoader
                    val context = chain.args[1] as? Context
                    if (pluginLoader != null && context != null) {
                        pluginHooked = true
                        hookPlugin(pluginLoader, requireNotNull(chain.thisObject), context)
                    }
                }
                original
            }
        }
    }

    private fun HookContext.hookInvokeSuspend(
        name: String,
        className: String,
        callback: (Any, Any?) -> Any?,
    ) {
        hookSafely(name) {
            val method = className.toClass().resolve()
                .firstMethod { this.name = "invokeSuspend"; parameterCount = 1 }.self
            xposed.hook(method).intercept { chain ->
                val original = chain.proceed()
                callback(requireNotNull(chain.thisObject), original)
            }
        }
    }

    private fun HookContext.hookPlugin(
        classLoader: ClassLoader,
        controller: Any,
        context: Context,
    ) {
        runCatching {
            val blurMethod = classLoader.loadClass("miui.systemui.util.MiBlurCompat").resolve()
                .firstMethod {
                    name = "getBackgroundMaterialOpenedInDefaultTheme"
                    parameterCount = 1
                }.self
            xposed.hook(blurMethod).intercept { true }

            val pluginBionics = classLoader
                .loadClass("miui.systemui.util.MaterialMode\$Bionics")
                .staticField("INSTANCE")
            val materialMethod = classLoader.loadClass("miui.systemui.util.MiBackgroundStyle")
                .resolve()
                .firstMethod {
                    name = "getMaterialMode"
                    emptyParameters()
                }.self
            xposed.hook(materialMethod).intercept { pluginBionics }

            val root = controller.field("content")?.call("getView") as? View
            root?.postDelayed({ refreshControls(root) }, 500)
            toggleBackgroundBlur(context)
        }.onFailure { Log.e(TAG, "Unable to hook control-center plugin", it) }
    }

    private fun toggleBackgroundBlur(context: Context) {
        runCatching {
            Settings.Secure.putInt(context.contentResolver, "background_blur_enable", 0)
            Handler(Looper.getMainLooper()).postDelayed({
                runCatching {
                    Settings.Secure.putInt(context.contentResolver, "background_blur_enable", 1)
                }
            }, 1000)
        }.onFailure { Log.w(TAG, "Unable to refresh background blur setting", it) }
    }

    private fun refreshControls(view: View) {
        when {
            view.javaClass.name == "miui.systemui.controlcenter.qs.tileview.QSCardItemView" -> {
                runCatching {
                    val state = view.field("state") ?: return@runCatching
                    val connected = view.field("connected") as? Boolean ?: false
                    view.call("updateState", state, connected, true)
                }
            }

            view.javaClass.name.contains("ToggleSlider") -> {
                runCatching { view.call("updateBlendBlur", true) }
                    .recoverCatching {
                        view.call(
                            "onConfigurationChanged",
                            view.resources.configuration
                        )
                    }
            }
        }
        if (view is ViewGroup) {
            repeat(view.childCount) { refreshControls(view.getChildAt(it)) }
        }
    }
}
