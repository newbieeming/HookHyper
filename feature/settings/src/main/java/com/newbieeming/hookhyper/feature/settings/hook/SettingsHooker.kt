package com.newbieeming.hookhyper.feature.settings.hook

import android.util.Log
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.newbieeming.hookhyper.core.model.PreferenceKeys
import com.newbieeming.hookhyper.feature.settings.SettingsFeatureEntry
import com.newbieeming.hookhyper.feature.settings.model.DeviceInfoFields
import com.newbieeming.hookhyper.feature.settings.model.SettingsPreferenceKeys
import java.lang.reflect.Method

object SettingsHooker : YukiBaseHooker() {
    private const val TAG = "HookHyper-Settings"
    private const val ABOUT_PHONE = "com.android.settings.device.MiuiAboutPhoneUtils"
    private const val BASE_CARD = "com.android.settings.device.BaseDeviceCardItem"
    private const val CARD_INFO = "com.android.settings.device.DeviceCardInfo"

    override fun onHook() {
        loadApp(name = SettingsFeatureEntry.PACKAGE_NAME) {
            val featurePreferences = prefs(PreferenceKeys.FILE_NAME)
            if (!featurePreferences.getBoolean(SettingsPreferenceKeys.EDIT_DEVICE_INFO)) return@loadApp

            val deviceNameKey = DeviceInfoFields.deviceName.preferenceKey
            val osVersionKey = DeviceInfoFields.osVersion.preferenceKey

            runCatching {
                ABOUT_PHONE.toClass().resolve().firstMethod {
                    name = "getDeviceMarketName"
                    emptyParameters()
                }.hook {
                    after {
                        featurePreferences.getString(deviceNameKey).takeIf(String::isNotBlank)?.let {
                            result = it
                        }
                    }
                }
            }.onFailure { Log.e(TAG, "Unable to hook device market name", it) }

            runCatching {
                ABOUT_PHONE.toClass().resolve().firstMethod {
                    name = "getOsVersionCode"
                    emptyParameters()
                }.hook {
                    after {
                        featurePreferences.getString(osVersionKey).takeIf(String::isNotBlank)?.let {
                            result = it
                        }
                    }
                }
            }.onFailure { Log.e(TAG, "Unable to hook OS version", it) }

            runCatching {
                BASE_CARD.toClass().resolve().firstMethod {
                    name = "setValue"
                    parameters(CharSequence::class)
                }.hook {
                    before {
                        val original = args[0]?.toString().orEmpty()
                        val key = DeviceInfoFields.preferenceKeyForValue(original) ?: return@before
                        featurePreferences.getString(key).takeIf(String::isNotBlank)?.let {
                            args(index = 0).set(it)
                        }
                    }
                }
            }.onFailure { Log.e(TAG, "Unable to hook CharSequence device card", it) }

            runCatching {
                val cardInfo = CARD_INFO.toClass()
                BASE_CARD.toClass().resolve().optional(silent = true).firstMethodOrNull {
                    name = "setValue"
                    parameters(cardInfo)
                }?.hook {
                    before { updateCardInfo(args.firstOrNull(), featurePreferences::getString) }
                }

                BASE_CARD.toClass().resolve().optional(silent = true).method {
                    name = "setValue"
                    parameterCount = 2
                }.hookAll {
                    before { updateCardInfo(args.firstOrNull(), featurePreferences::getString) }
                }
            }.onFailure { Log.e(TAG, "Unable to hook structured device cards", it) }
        }
    }

    private fun updateCardInfo(
        cardInfo: Any?,
        read: (String, String) -> String,
    ) {
        if (cardInfo == null) return
        runCatching {
            val title = cardInfo.call("getTitle")?.toString()?.trim().orEmpty()
            val key = DeviceInfoFields.preferenceKeyForTitle(title) ?: return
            val replacement = read(key, "").takeIf(String::isNotBlank) ?: return
            cardInfo.call("setValue", replacement)
            if (cardInfo.call("getFirstValue") != null) {
                cardInfo.call("setFirstValue", replacement)
                cardInfo.call("setSecondValue", "")
            }
        }.onFailure { Log.e(TAG, "Unable to update device card", it) }
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
