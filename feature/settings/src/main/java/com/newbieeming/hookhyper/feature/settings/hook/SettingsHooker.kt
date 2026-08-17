package com.newbieeming.hookhyper.feature.settings.hook

import android.util.Log
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.newbieeming.hookhyper.core.model.PreferenceKeys
import com.newbieeming.hookhyper.feature.settings.SettingsFeatureEntry
import com.newbieeming.hookhyper.feature.settings.model.DeviceInfoFields
import com.newbieeming.hookhyper.feature.settings.model.DeviceInfoFields.osVersion
import com.newbieeming.hookhyper.feature.settings.model.SettingsPreferenceKeys
import java.lang.reflect.Method

object SettingsHooker : YukiBaseHooker() {
    private const val TAG = "HookHyper-Settings"
    private const val CARD_INFO = "com.android.settings.device.DeviceCardInfo"
    private const val BASE_CARD = "com.android.settings.device.BaseDeviceCardItem"
    private const val ABOUT_PHONE = "com.android.settings.device.MiuiAboutPhoneUtils"

    override fun onHook() {
        loadApp(name = SettingsFeatureEntry.PACKAGE_NAME) {
            val featurePreferences = prefs(PreferenceKeys.FILE_NAME)
            if (!featurePreferences.getBoolean(SettingsPreferenceKeys.EDIT_DEVICE_INFO)) return@loadApp

            runCatching {
                ABOUT_PHONE.toClass().resolve().firstMethod {
                    name = "getOsVersionCode"
                    emptyParameters()
                }.hook {
                    after {
                        featurePreferences.getString(osVersion.preferenceKey).takeIf(String::isNotBlank)?.let {
                            result = it
                        }
                    }
                }
            }.onFailure { Log.e(TAG, "Unable to hook OS version", it) }

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

    private val resolveString: (String) -> String? = { name ->
        runCatching {
            val appRes = appResources
            val resId = appRes?.getIdentifier(
                name,
                "string",
                SettingsFeatureEntry.PACKAGE_NAME
            ) ?: 0
            if (resId != 0) appRes?.getString(resId) else null
        }.getOrNull()
    }

    private fun Any.call(name: String, vararg arguments: Any?): Any? =
        javaClass.findMethod(name, arguments).invoke(this, *arguments)

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
