package com.newbieeming.hookhyper.feature.systemui.hook

import android.content.res.Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef

@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class TimeFormatHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {

    override val def = SystemUiHookDef.CUSTOM_TIME_FORMAT

    @Composable
    override fun Content() {
        val repo = LocalPreferencesRepository.current
        var showSub by remember { mutableStateOf(repo.getBoolean(preferenceKey)) }
        HookSwitchPreference(
            preferenceKey = preferenceKey,
            title = stringResource(R.string.systemui_custom_time_format_title),
            summary = stringResource(R.string.systemui_custom_time_format_summary),
            onCheckedChange = { showSub = it },
        )
        AnimatedVisibility(
            visible = showSub,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            HookSwitchPreference(
                preferenceKey = SystemUiHookDef.TIME_FORMAT_AA_PREFIX.preferenceKey,
                title = stringResource(R.string.systemui_aa_prefix_title),
                summary = stringResource(R.string.systemui_aa_prefix_summary),
            )
        }
    }

    override fun PackageParam.onHook() {
        val featurePreferences = prefs(PreferenceKeys.FILE_NAME)
        val aaPrefix = featurePreferences.getBoolean(SystemUiHookDef.TIME_FORMAT_AA_PREFIX.preferenceKey)
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
                    "fmt_time_24hour_minute",
                    -> {
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
