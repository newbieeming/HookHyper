package com.newbieeming.hookhyper.feature.systemui.hook

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.newbieeming.hookhyper.core.model.PreferenceKeys
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import java.lang.reflect.Field
import java.lang.reflect.Method

object SystemUiHooker : YukiBaseHooker() {
    private const val TAG = "HookHyper-SystemUI"

    override fun onHook() {
        loadApp(name = SystemUiFeatureEntry.PACKAGE_NAME) {
            val featurePreferences = prefs(PreferenceKeys.FILE_NAME)
            if (featurePreferences.getBoolean(PreferenceKeys.SYSTEMUI_LOCK_SHOW_SIM_NAME)) {
                runCatching { hookLockScreenCarrier() }
                    .onFailure { Log.e(TAG, "Unable to hook lock-screen carrier", it) }
            }
            if (featurePreferences.getBoolean(PreferenceKeys.SYSTEMUI_FORCE_SOFT_LIGHT_GLASS)) {
                runCatching { hookSoftLightGlass() }
                    .onFailure { Log.e(TAG, "Unable to hook soft-light glass", it) }
            }
        }
    }

    private fun com.highcapable.yukihookapi.hook.param.PackageParam.hookLockScreenCarrier() {
        "com.android.systemui.statusbar.phone.KeyguardStatusBarView".toClass().resolve()
            .firstMethod {
                name = "onFinishInflate"
                emptyParameters()
            }.hook {
                after {
                    val root = instance<View>()
                    val carrierId = root.resources.getIdentifier(
                        "keyguard_carrier_text",
                        "id",
                        SystemUiFeatureEntry.PACKAGE_NAME,
                    )
                    root.findViewById<TextView>(carrierId)?.let { carrier ->
                        carrier.text = carrier.text.toString().substringBefore("|").trim()
                        carrier.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun com.highcapable.yukihookapi.hook.param.PackageParam.hookSoftLightGlass() {
        val materialType = "com.miui.systemui.material.MaterialType".toClass()
        val blur = materialType.staticField("BLUR")
        val glass = materialType.staticField("GLASS")

        "com.android.systemui.statusbar.notification.style.domain.NotificationMaterialStateInteractor\$materialTypeState\$1"
            .toClass().resolve()
            .firstMethod {
                name = "invokeSuspend"
                parameterCount = 1
            }.hook {
                after {
                    if (result<Any>() == blur) result = glass
                }
            }

        val bionics = "com.miui.interfaces.controlcenter.data.repository.MaterialMode\$Bionics"
            .toClass().staticField("INSTANCE")
        "com.miui.systemui.shade.blur.ShadeBlendBlurController\$isBionicsEnabled\$1"
            .toClass().resolve()
            .firstMethod {
                name = "invokeSuspend"
                parameterCount = 1
            }.hook {
                after {
                    if (instance.field("L\$0") == bionics) resultTrue()
                }
            }

        var pluginHooked = false
        "com.miui.systemui.controlcenter.container.ControlCenterContentController".toClass().resolve()
            .firstMethod {
                name = "onPluginLoaded"
                parameterCount = 3
            }.hook {
                after {
                    if (pluginHooked) return@after
                    val plugin = args[0] ?: return@after
                    val pluginClassLoader = plugin.javaClass.classLoader ?: return@after
                    val context = args[1] as? Context ?: return@after
                    pluginHooked = true

                    runCatching {
                        pluginClassLoader.loadClass("miui.systemui.util.MiBlurCompat").resolve()
                            .firstMethod {
                                name = "getBackgroundMaterialOpenedInDefaultTheme"
                                parameterCount = 1
                            }.hook { replaceToTrue() }

                        val pluginBionics = pluginClassLoader
                            .loadClass("miui.systemui.util.MaterialMode\$Bionics")
                            .staticField("INSTANCE")
                        pluginClassLoader.loadClass("miui.systemui.util.MiBackgroundStyle").resolve()
                            .firstMethod {
                                name = "getMaterialMode"
                                emptyParameters()
                            }.hook { replaceTo(pluginBionics) }

                        val content = instance.field("content")
                        val root = content?.call("getView") as? View
                        root?.postDelayed({ refreshControls(root) }, 500)
                        toggleBackgroundBlur(context)
                    }.onFailure { Log.e(TAG, "Unable to hook control-center plugin", it) }
                }
            }
    }

    private fun toggleBackgroundBlur(context: Context) {
        runCatching {
            Settings.Secure.putInt(context.contentResolver, "background_blur_enable", 0)
            Handler(Looper.getMainLooper()).postDelayed({
                runCatching {
                    Settings.Secure.putInt(context.contentResolver, "background_blur_enable", 1)
                }
            }, 500)
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
                    .recoverCatching { view.call("onConfigurationChanged", view.resources.configuration) }
            }
        }
        if (view is ViewGroup) {
            repeat(view.childCount) { refreshControls(view.getChildAt(it)) }
        }
    }

    private fun Class<*>.staticField(name: String): Any? = findField(name).get(null)

    private fun Any.field(name: String): Any? = javaClass.findField(name).get(this)

    private fun Any.call(name: String, vararg arguments: Any?): Any? =
        javaClass.findMethod(name, arguments).invoke(this, *arguments)

    private fun Class<*>.findField(name: String): Field {
        var current: Class<*>? = this
        while (current != null) {
            runCatching { current.getDeclaredField(name) }.getOrNull()?.let {
                it.isAccessible = true
                return it
            }
            current = current.superclass
        }
        error("Field $name not found in $this")
    }

    private fun Class<*>.findMethod(name: String, arguments: Array<out Any?>): Method {
        var current: Class<*>? = this
        while (current != null) {
            current.declaredMethods.firstOrNull { method ->
                method.name == name && method.parameterCount == arguments.size
            }?.let {
                it.isAccessible = true
                return it
            }
            current = current.superclass
        }
        error("Method $name/${arguments.size} not found in $this")
    }
}
