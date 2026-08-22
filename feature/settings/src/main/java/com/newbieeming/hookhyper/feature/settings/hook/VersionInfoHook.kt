package com.newbieeming.hookhyper.feature.settings.hook

import android.util.Log
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.feature.settings.SettingsFeatureEntry
import com.newbieeming.hookhyper.feature.settings.model.DeviceInfoFields
import com.newbieeming.hookhyper.feature.settings.model.SettingsPreferenceKeys

@HookModule(
    packageName = SettingsFeatureEntry.PACKAGE_NAME,
    preferenceKey = SettingsPreferenceKeys.EDIT_DEVICE_INFO,
)
class VersionInfoHook : SubHooker {

    override val preferenceKey = SettingsPreferenceKeys.EDIT_DEVICE_INFO

    private companion object {
        const val TAG = "VersionInfoHook"
        const val ABOUT_PHONE = "com.android.settings.device.MiuiAboutPhoneUtils"
    }

    override fun PackageParam.onHook() {
        val featurePreferences = prefs(PreferenceKeys.FILE_NAME)

        runCatching {
            ABOUT_PHONE.toClass().resolve().firstMethod {
                name = "getOsVersionCode"
                emptyParameters()
            }.hook {
                after {
                    featurePreferences.getString(DeviceInfoFields.osVersion.preferenceKey)
                        .takeIf(String::isNotBlank)?.let { result = it }
                }
            }
            ABOUT_PHONE.toClass().resolve().firstMethod {
                name = "getRoXmsVersion"
                emptyParameters()
            }.hook {
                after {
                    featurePreferences.getString(DeviceInfoFields.roXmsVersion.preferenceKey)
                        .takeIf(String::isNotBlank)?.let { result = it }
                }
            }
            ABOUT_PHONE.toClass().resolve().firstMethod {
                name = "getXmsVersion"
                emptyParameters()
            }.hook {
                after {
                    featurePreferences.getString(DeviceInfoFields.xmsVersion.preferenceKey)
                        .takeIf(String::isNotBlank)?.let { result = it }
                }
            }
        }.onFailure { Log.e(TAG, "Unable to hook OS version", it) }
    }
}
