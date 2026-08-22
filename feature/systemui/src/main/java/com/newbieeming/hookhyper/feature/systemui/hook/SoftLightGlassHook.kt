package com.newbieeming.hookhyper.feature.systemui.hook

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.HookUtils.call
import com.newbieeming.hookhyper.core.hook.HookUtils.field
import com.newbieeming.hookhyper.core.hook.HookUtils.staticField
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiPreferenceKeys

@HookModule(
    packageName = SystemUiFeatureEntry.PACKAGE_NAME,
    preferenceKey = SystemUiPreferenceKeys.FORCE_SOFT_LIGHT_GLASS,
)
class SoftLightGlassHook : SubHooker {

    override val preferenceKey = SystemUiPreferenceKeys.FORCE_SOFT_LIGHT_GLASS

    private companion object {
        private const val TAG = "SoftLightGlassHook"
    }

    override fun PackageParam.onHook() {
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
}
