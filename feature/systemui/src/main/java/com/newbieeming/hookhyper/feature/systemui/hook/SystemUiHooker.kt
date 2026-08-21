package com.newbieeming.hookhyper.feature.systemui.hook

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.newbieeming.hookhyper.core.model.PreferenceKeys
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiPreferenceKeys
import java.lang.reflect.Field
import java.lang.reflect.Method

object SystemUiHooker : YukiBaseHooker() {
    private const val TAG = "HookHyper-SystemUI"

    override fun onHook() {
        loadApp(name = SystemUiFeatureEntry.PACKAGE_NAME) {
            val featurePreferences = prefs(PreferenceKeys.FILE_NAME)
            if (featurePreferences.getBoolean(SystemUiPreferenceKeys.LOCK_SHOW_SIM_NAME)) {
                runCatching { hookLockScreenCarrier() }
                    .onFailure { Log.e(TAG, "Unable to hook lock-screen carrier", it) }
            }
            if (featurePreferences.getBoolean(SystemUiPreferenceKeys.FORCE_SOFT_LIGHT_GLASS)) {
                runCatching { hookSoftLightGlass() }
                    .onFailure { Log.e(TAG, "Unable to hook soft-light glass", it) }
            }
            if (featurePreferences.getBoolean(SystemUiPreferenceKeys.CUSTOM_TIME_FORMAT)) {
                val aaPrefix = featurePreferences.getBoolean(SystemUiPreferenceKeys.TIME_FORMAT_AA_PREFIX)
                runCatching { hookTimeFormat(aaPrefix) }
                    .onFailure { Log.e(TAG, "Unable to hook time format", it) }
            }
            if (featurePreferences.getBoolean(SystemUiPreferenceKeys.REPLACE_FINGERPRINT_ICON)) {
                runCatching { hookFingerprintIcon() }
                    .onFailure { Log.e(TAG, "Unable to hook fingerprint icon", it) }
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

    private fun com.highcapable.yukihookapi.hook.param.PackageParam.hookTimeFormat(aaPrefix: Boolean) {
        val amPms12h = arrayOf("AM", "AM", "AM", "PM", "PM", "PM", "PM")

        Resources::class.java.resolve().firstMethod {
            name = "getString"
            parameterCount = 1
        }.hook {
            after {
                val res = instance<Resources>()
                val resId = args(0).int()
                val resName = runCatching {
                    res.getResourceEntryName(resId)
                }.getOrNull() ?: return@after
                when (resName) {
                    "fmt_time_12hour_minute",
                    "fmt_time_24hour_minute" -> {
                        val original = result?.toString().orEmpty()
                        if (!original.contains("aa", ignoreCase = true)) {
                            result = if (aaPrefix) "aa $original" else "$original aa"
                        }
                    }
                }
            }
        }

        Resources::class.java.resolve().firstMethod {
            name = "getStringArray"
            parameterCount = 1
        }.hook {
            after {
                val resId = args(0).int()
                val resName = runCatching {
                    instance<Resources>().getResourceEntryName(resId)
                }.getOrNull() ?: return@after
                if (resName == "detailed_am_pms") {
                    result = amPms12h
                }
            }
        }
    }

    private fun com.highcapable.yukihookapi.hook.param.PackageParam.hookFingerprintIcon() {
        val replacements = mapOf(
            "finger_circle_image_normal" to R.drawable.finger_circle_image_normal,
            "finger_circle_image_light" to R.drawable.finger_circle_image_light,
            "finger_circle_image_grey" to R.drawable.finger_circle_image_grey,
            "finger_circle_image_aod" to R.drawable.finger_circle_image_aod,
            "finger_circle_image_grey_enroll" to R.drawable.finger_circle_image_grey,
        )
        // 注入模块资源，使模块 R.drawable.xxx 在宿主中可用
        onAppLifecycle {
            onCreate {
                injectModuleAppResources()
            }
        }
        // hook getFingerIconResource 直接替换返回的资源 ID
        "com.miui.keyguard.biometrics.fod.MiuiGxzwAnimManager".toClass().resolve().firstMethod {
            name = "getFingerIconResource"
            parameterCount = 1
        }.hook {
            after {
                val original = result as? Int ?: return@after
                val res = appResources ?: return@after
                val resName = runCatching {
                    res.getResourceEntryName(original)
                }.getOrNull() ?: return@after
                replacements[resName]?.let { moduleResId ->
                    result = moduleResId
                    Log.d(TAG, "Replaced: $resName -> 0x${Integer.toHexString(moduleResId)}")
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
            }, 800)
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
