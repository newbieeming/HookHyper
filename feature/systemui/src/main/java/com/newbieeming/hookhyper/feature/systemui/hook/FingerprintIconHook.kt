package com.newbieeming.hookhyper.feature.systemui.hook

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef

@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class FingerprintIconHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {

    override val def = SystemUiHookDef.REPLACE_FINGERPRINT_ICON

    private companion object {
        private const val TAG = "FingerprintIconHook"
    }

    @Composable
    override fun Content() {
        HookSwitchPreference(
            preferenceKey = preferenceKey,
            title = stringResource(R.string.systemui_replace_fingerprint_icon_title),
            summary = stringResource(R.string.systemui_replace_fingerprint_icon_summary),
        )
    }

    override fun PackageParam.onHook() {
        val replacements = mapOf(
            "finger_circle_image_normal" to R.drawable.finger_circle_image_normal,
            "finger_circle_image_light" to R.drawable.finger_circle_image_light,
            "finger_circle_image_grey" to R.drawable.finger_circle_image_grey,
            "finger_circle_image_aod" to R.drawable.finger_circle_image_aod,
            "finger_circle_image_grey_enroll" to R.drawable.finger_circle_image_grey,
        )
        // 注入模块资源，使模块 R.drawable.xxx 在宿主中可用
        onAppLifecycle {
            onCreate {
                injectModuleAppResources()
            }
        }
        // hook getFingerIconResource 直接替换返回的资源 ID
        "com.miui.keyguard.biometrics.fod.MiuiGxzwAnimManager".toClass().resolve().firstMethod {
            name = "getFingerIconResource"
            parameterCount = 1
        }.hook {
            after {
                val original = result as? Int ?: return@after
                val res = appResources ?: return@after
                val resName = runCatching {
                    res.getResourceEntryName(original)
                }.getOrNull() ?: return@after
                replacements[resName]?.let { moduleResId ->
                    result = moduleResId
                    Log.d(TAG, "Replaced: $resName -> 0x${Integer.toHexString(moduleResId)}")
                }
            }
        }
    }
}
