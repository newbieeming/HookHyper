package com.newbieeming.hookhyper.feature.settings.hook

import android.util.Log
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.HookUtils.call
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.feature.settings.SettingsFeatureEntry
import com.newbieeming.hookhyper.feature.settings.model.DeviceInfoFields
import com.newbieeming.hookhyper.feature.settings.model.SettingsHookDef

@HookModule(
    packageName = SettingsFeatureEntry.PACKAGE_NAME,
    preferenceKey = "settings_edit_device_info",
)
class DeviceCardHook : SubHooker {

    override val preferenceKey = SettingsHookDef.EDIT_DEVICE_INFO.preferenceKey

    private companion object {
        const val TAG = "DeviceCardHook"
        const val CARD_INFO = "com.android.settings.device.DeviceCardInfo"
        const val BASE_CARD = "com.android.settings.device.BaseDeviceCardItem"
    }

    override fun PackageParam.onHook() {
        val featurePreferences = prefs(PreferenceKeys.FILE_NAME)
        val resolveString: (String) -> String? = { name ->
            runCatching {
                val appRes = appResources
                val resId = appRes?.getIdentifier(
                    name,
                    "string",
                    SettingsFeatureEntry.PACKAGE_NAME,
                ) ?: 0
                if (resId != 0) appRes?.getString(resId) else null
            }.getOrNull()
        }

        runCatching {
            // 特殊处理摄像头FirstValue、SecondValue
            BASE_CARD.toClass().resolve().optional(silent = true).method {
                name = "setValue"
                parameterCount = 2
            }.hookAll {
                before {
                    val info = args[0]
                    val title = info?.call("getTitle")?.toString().orEmpty()
                    val str = resolveString(DeviceInfoFields.camera.stringsName)
                    if (title != str) return@before
                    val value = info?.call("getValue")?.toString().orEmpty()
                    info?.call("setValue", value)
                    if (info?.call("getFirstValue") != null) {
                        info.call("setFirstValue", value)
                        info.call("setSecondValue", "")
                    }
                }
            }
        }.onFailure { Log.e(TAG, "Unable to hook structured device cards", it) }

        runCatching {
            CARD_INFO.toClass().resolve().firstMethod {
                name = "setValue"
                parameters(String::class)
            }.hook {
                before {
                    val cardInfo = instance
                    val title = cardInfo.call("getTitle")?.toString()?.trim().orEmpty()
                    val value = args[0]?.toString().orEmpty()
                    val key = DeviceInfoFields.resolveKey(title, value, resolveString)
                        ?: return@before
                    featurePreferences.getString(key).takeIf(String::isNotBlank)?.let {
                        args(index = 0).set(it)
                    }
                }
            }
        }.onFailure { Log.e(TAG, "Unable to hook DeviceCardInfo.setValue", it) }
    }
}
