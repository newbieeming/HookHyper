package com.newbieeming.hookhyper.feature.systemui.hook

import android.view.View
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef

@HookModule(
    packageName = SystemUiFeatureEntry.PACKAGE_NAME,
    preferenceKey = "systemui_lock_show_sim_name",
)
class LockScreenCarrierHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {

    override val def = SystemUiHookDef.LOCK_SHOW_SIM_NAME

    @Composable
    override fun Content() {
        HookSwitchPreference(
            preferenceKey = preferenceKey,
            title = stringResource(R.string.systemui_show_carrier_title),
            summary = stringResource(R.string.systemui_show_carrier_summary),
        )
    }

    override fun PackageParam.onHook() {
        "com.android.systemui.statusbar.phone.KeyguardStatusBarView".toClass().resolve()
            .firstMethod {
                name = "onFinishInflate"
                emptyParameters()
            }.hook {
                after {
                    val root = instance<View>()
                    val carrierId = root.resources.getIdentifier(
                        "keyguard_carrier_text",
                        "id",
                        SystemUiFeatureEntry.PACKAGE_NAME,
                    )
                    root.findViewById<TextView>(carrierId)?.let { carrier ->
                        carrier.text = carrier.text.toString().substringBefore("|").trim()
                        carrier.visibility = View.VISIBLE
                    }
                }
            }
    }
}
