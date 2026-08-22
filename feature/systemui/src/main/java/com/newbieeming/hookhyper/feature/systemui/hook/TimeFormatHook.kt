package com.newbieeming.hookhyper.feature.systemui.hook

import android.content.res.Resources
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiPreferenceKeys

@HookModule(
    packageName = SystemUiFeatureEntry.PACKAGE_NAME,
    preferenceKey = SystemUiPreferenceKeys.CUSTOM_TIME_FORMAT,
)
class TimeFormatHook : SubHooker {

    override val preferenceKey = SystemUiPreferenceKeys.CUSTOM_TIME_FORMAT

    override fun PackageParam.onHook() {
        val featurePreferences = prefs(PreferenceKeys.FILE_NAME)
        val aaPrefix = featurePreferences.getBoolean(SystemUiPreferenceKeys.TIME_FORMAT_AA_PREFIX)
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
}
