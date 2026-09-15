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
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.core.ui.component.SettingsPreferenceGroup
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
        SettingsPreferenceGroup {
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
    }

    override fun PackageParam.onHook() {
        val featurePreferences = prefs(PreferenceKeys.FILE_NAME)
        val aaPrefix = featurePreferences.getBoolean(SystemUiHookDef.TIME_FORMAT_AA_PREFIX.preferenceKey)
        val amPms12h = arrayOf("AM", "AM", "AM", "PM", "PM", "PM", "PM")
        val resourcesClass = Resources::class.java.resolve()
        val entryName: Resources.(Int) -> String? = { runCatching { getResourceEntryName(it) }.getOrNull() }

        mapOf(
            "getString" to fun HookParam.() {
                val resName = instance<Resources>().entryName(args(0).int()) ?: return
                if (resName in TIME_RELATED_RESOURCES) {
                    val original = result?.toString().orEmpty()
                    if (!original.contains("aa", ignoreCase = true)) {
                        result = if (aaPrefix) "aa $original" else "$original aa"
                    }
                }
            },
            "getStringArray" to fun HookParam.() {
                val resName = instance<Resources>().entryName(args(0).int()) ?: return
                if (resName == "detailed_am_pms") result = amPms12h
            },
        ).forEach { (methodName, callback) ->
            hookSafely("Resources.$methodName") {
                resourcesClass.firstMethod {
                    name = methodName
                    parameterCount = 1
                }.hook { after(callback) }
            }
        }
    }

    private companion object {
        val TIME_RELATED_RESOURCES = setOf("fmt_time_12hour_minute", "fmt_time_24hour_minute")
    }
}
