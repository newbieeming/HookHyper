package com.newbieeming.hookhyper.feature.systemui.hook

import android.view.View
import android.widget.TextView
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiPreferenceKeys

@HookModule(
    packageName = SystemUiFeatureEntry.PACKAGE_NAME,
    preferenceKey = SystemUiPreferenceKeys.LOCK_SHOW_SIM_NAME,
)
class LockScreenCarrierHook : SubHooker {

    override val preferenceKey = SystemUiPreferenceKeys.LOCK_SHOW_SIM_NAME

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
