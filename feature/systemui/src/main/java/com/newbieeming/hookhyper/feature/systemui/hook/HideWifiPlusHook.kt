package com.newbieeming.hookhyper.feature.systemui.hook

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.HookUtils.findField
import com.newbieeming.hookhyper.core.hook.HookUtils.staticField
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.SettingsPreferenceGroup
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef
import com.newbieeming.hookhyper.feature.systemui.model.WifiIconRules

@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class HideWifiPlusHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {
    override val def = SystemUiHookDef.HIDE_WIFI_PLUS

    @Composable
    override fun Content() {
        SettingsPreferenceGroup {
            HookSwitchPreference(
                preferenceKey = preferenceKey,
                title = stringResource(R.string.systemui_hide_wifi_plus_title),
                summary = stringResource(R.string.systemui_hide_wifi_plus_summary),
            )
        }
    }

    override fun HookContext.onHook() {
        hookSafely("WifiIcon.fromModel") {
            val icons = "com.android.systemui.statusbar.connectivity.MiuiWifiIcons".toClass()
            val plusIcons = (icons.staticField("WIFI_MLO_ICONS") as IntArray).copyOf()
            val normalIcons = (icons.staticField("WIFI_FULL_ICONS") as IntArray).copyOf()
            val hotspotIcons = (icons.staticField("WIFI_HOTSPOT_ICONS") as IntArray).copyOf()
            check(plusIcons.size == normalIcons.size && plusIcons.size == hotspotIcons.size)
            val visibleClass = "$WIFI_ICON_CLASS\$Visible".toClass()
            val descriptionClass = "com.android.systemui.common.shared.model.ContentDescription\$Loaded".toClass()
            val constructor = visibleClass.getDeclaredConstructor(Int::class.javaPrimitiveType, descriptionClass)
            val iconField = visibleClass.findField("icon")
            val descriptionField = visibleClass.findField("contentDescription")
            val resourceField = "com.android.systemui.common.shared.model.Icon\$Resource".toClass().findField("resId")
            val modelClass = WIFI_MODEL_CLASS.toClass()
            val activeClass = "$WIFI_MODEL_CLASS\$Active".toClass()
            val extensionField = activeClass.findField("ext")
            val meteredField = "com.android.systemui.statusbar.pipeline.MiuiWifiExt".toClass().findField("meteredHint")
            val method = "$WIFI_ICON_CLASS\$Companion".toClass().getDeclaredMethod(
                "fromModel",
                modelClass,
                Context::class.java,
                Boolean::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
            )
            xposed.hook(method).intercept { chain ->
                val original = chain.proceed()
                val model = chain.args[0]
                if (visibleClass.isInstance(original) && activeClass.isInstance(model)) {
                    runCatching {
                        val id = resourceField.getInt(iconField.get(original))
                        val metered = meteredField.getBoolean(extensionField.get(model))
                        // Bypass only the MLO/slave-Wi-Fi icon branch, preserving the metered fallback.
                        val replacement = WifiIconRules.withoutPlus(id, plusIcons, normalIcons, hotspotIcons, metered)
                        if (replacement == null) original else constructor.newInstance(replacement, descriptionField.get(original))
                    }.onFailure { Log.e(TAG, "Unable to select default Wi-Fi icon", it) }
                        .getOrDefault(original)
                } else {
                    original
                }
            }
        }
    }

    private companion object {
        const val TAG = "HookHyper-HideWifiPlus"
        const val WIFI_ICON_CLASS = "com.android.systemui.statusbar.pipeline.wifi.ui.model.WifiIcon"
        const val WIFI_MODEL_CLASS = "com.android.systemui.statusbar.pipeline.wifi.shared.model.WifiNetworkModel"
    }
}
