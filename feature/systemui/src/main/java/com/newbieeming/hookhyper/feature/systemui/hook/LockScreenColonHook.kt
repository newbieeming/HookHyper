package com.newbieeming.hookhyper.feature.systemui.hook

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.SettingsPreferenceGroup
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef
import io.github.libxposed.api.XposedInterface.ExceptionMode

@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class LockScreenColonHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {

    override val def = SystemUiHookDef.FORCE_CLOCK_COLON

    @Composable
    override fun Content() {
        SettingsPreferenceGroup(0,2) {
            HookSwitchPreference(
                preferenceKey = preferenceKey,
                title = stringResource(R.string.systemui_force_clock_colon_title),
                summary = stringResource(R.string.systemui_force_clock_colon_summary),
            )
        }
    }

    override fun HookContext.onHook() {
        hookSafely(IS_COLON_SHOW_METHOD) {
            val method = CLOCK_BEAN_CLASS.toClass().resolve()
                .firstMethod {
                    name = IS_COLON_SHOW_METHOD
                    emptyParameters()
                }
                .self
            xposed.hook(method)
                .setExceptionMode(ExceptionMode.PROTECTIVE)
                .intercept { true }
        }
    }

    private companion object {
        const val CLOCK_BEAN_CLASS = "com.miui.clock.module.ClockBean"
        const val IS_COLON_SHOW_METHOD = "isColonShow"
    }
}
