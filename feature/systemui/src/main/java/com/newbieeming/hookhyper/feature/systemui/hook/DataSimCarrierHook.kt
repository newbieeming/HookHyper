package com.newbieeming.hookhyper.feature.systemui.hook

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.HookUtils.field
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.core.ui.component.SettingsPreferenceGroup
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.DataSimCarrierSettings
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef

@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class DataSimCarrierHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {
    override val def = SystemUiHookDef.DATA_SIM_CARRIER_ONLY

    @Composable
    override fun Content() {
        val repository = LocalPreferencesRepository.current
        var enabled by remember { mutableStateOf(repository.getBoolean(preferenceKey)) }
        SettingsPreferenceGroup {
            HookSwitchPreference(
                preferenceKey = preferenceKey,
                title = stringResource(R.string.systemui_data_sim_carrier_only_title),
                summary = stringResource(R.string.systemui_data_sim_carrier_only_summary),
                onCheckedChange = { enabled = it },
            )
            AnimatedVisibility(visible = enabled) {
                HookSwitchPreference(
                    preferenceKey = DataSimCarrierSettings.NOTIFICATION_BAR,
                    title = stringResource(R.string.systemui_data_sim_carrier_notification_bar_title),
                    summary = stringResource(R.string.systemui_data_sim_carrier_notification_bar_summary),
                )
            }
        }
    }

    override fun HookContext.onHook() {
        val notificationBar = prefs(PreferenceKeys.FILE_NAME).getBoolean(DataSimCarrierSettings.NOTIFICATION_BAR)
        hookSafely("MiuiCarrierTextController") {
            val controllerClass = CONTROLLER_CLASS.toClass()
            val slotMethod = SubscriptionManager::class.java.getDeclaredMethod("getSlotIndex", Int::class.javaPrimitiveType)
                .apply { isAccessible = true }
            val views = DataSimCarrierViews(notificationBar) {
                slotMethod.invoke(null, SubscriptionManager.getDefaultDataSubscriptionId()) as Int
            }
            hookSafely("ControlCenterCarrierText") {
                views.install(this, "com.android.systemui.controlcenter.shade.ControlCenterCarrierText", isInjector = false)
            }
            hookSafely("CarrierTextInjector") {
                views.install(this, "com.android.systemui.statusbar.ui.viewmodel.CarrierTextInjector", isInjector = true)
            }
            hookDataSimChanges(controllerClass)
        }
    }

    private fun HookContext.hookDataSimChanges(controllerClass: Class<*>) {
        controllerClass.declaredConstructors.forEach { constructor ->
            hookSafely("data SIM change receiver") {
                xposed.hook(constructor).intercept { chain ->
                    val result = chain.proceed()
                    runCatching { registerDataSimReceiver(checkNotNull(chain.thisObject)) }
                        .onFailure { Log.e(TAG, "Unable to observe data SIM changes", it) }
                    result
                }
            }
        }
    }

    private fun registerDataSimReceiver(controller: Any) {
        val context = (controller.field("mContext") as Context).applicationContext
        val handler = controller.field("mMainHandler") as Handler
        val refresh = controller.field("updateCarrierTextRunnable") as Runnable
        // The controller and receiver share the SystemUI process lifetime; no View is retained.
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == DEFAULT_DATA_CHANGED_ACTION) {
                    handler.post {
                        runCatching {
                            // Force unchanged carrier labels to be dispatched after a data SIM switch.
                            val carriers = controller.field("mCurrentCarrier") as Array<*>
                            carriers.indices.forEach { java.lang.reflect.Array.set(carriers, it, null) }
                            refresh.run()
                        }.onFailure { Log.e(TAG, "Unable to refresh data SIM carrier views", it) }
                    }
                }
            }
        }
        context.registerReceiver(
            receiver,
            IntentFilter(DEFAULT_DATA_CHANGED_ACTION),
            null,
            handler,
        )
    }

    private companion object {
        const val DEFAULT_DATA_CHANGED_ACTION = "android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED"
        const val TAG = "HookHyper-DataSimCarrier"
        const val CONTROLLER_CLASS = "com.android.systemui.statusbar.policy.MiuiCarrierTextController"
    }
}
