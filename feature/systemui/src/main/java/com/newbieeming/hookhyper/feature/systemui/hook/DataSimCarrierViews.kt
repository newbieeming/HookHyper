package com.newbieeming.hookhyper.feature.systemui.hook

import android.util.Log
import android.view.View
import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.core.hook.HookUtils.call
import com.newbieeming.hookhyper.core.hook.HookUtils.field
import com.newbieeming.hookhyper.feature.systemui.model.DataSimCarrierSettings
import com.newbieeming.hookhyper.feature.systemui.model.DataSimCarrierSlots
import io.github.libxposed.api.XposedInterface.ExceptionMode

/** Filters presentation per surface while keeping the controller's full SIM data intact. */
internal class DataSimCarrierViews(
    private val notificationBar: Boolean,
    private val dataSlot: () -> Int,
) {
    fun install(context: HookContext, className: String, isInjector: Boolean) {
        hookText(context, className, isInjector)
        hookHd(context, className, isInjector)
    }

    private fun hookText(context: HookContext, className: String, isInjector: Boolean) = with(context) {
        val callbackClass = (className + if (isInjector) "\$carrierTextCallback\$1" else "\$mCarrierTextCallback\$1").toClass()
        val ownerField = callbackClass.getDeclaredField("this\$0").apply { isAccessible = true }
        val callback = callbackClass.getDeclaredMethod(
            "onCarrierTextChanged",
            Int::class.javaPrimitiveType,
            String::class.java,
            Int::class.javaPrimitiveType,
        )
        xposed.hook(callback).setExceptionMode(ExceptionMode.PROTECTIVE).intercept { chain ->
            val owner = checkNotNull(ownerField.get(chain.thisObject))
            val original = chain.args.toTypedArray()
            val arguments = runCatching { filteredArguments(owner, original, isInjector) }.getOrDefault(original)
            if (arguments.isEmpty()) {
                null
            } else {
                val result = chain.proceed(arguments)
                if (isInjector && arguments !== original) {
                    runCatching { refreshInjector(owner, arguments[0] == 0) }
                        .onFailure { Log.e(TAG, "Unable to refresh carrier drawable", it) }
                }
                result
            }
        }
    }

    private fun filteredArguments(owner: Any, arguments: Array<Any?>, isInjector: Boolean): Array<Any?> {
        val selected = selectedSlot(owner, isInjector)
        return DataSimCarrierSettings.callbackArguments(arguments, selected, owner.field(SLOT_FIELD) as Int)
    }

    private fun hookHd(context: HookContext, className: String, isInjector: Boolean) = with(context) {
        val viewClass = className.toClass()
        val hdMethod = if (isInjector) {
            viewClass.getDeclaredMethod("updateHDDrawable", Int::class.javaPrimitiveType)
        } else {
            viewClass.getDeclaredMethod("updateHDText", Boolean::class.javaPrimitiveType, Boolean::class.javaPrimitiveType)
        }
        xposed.hook(hdMethod).setExceptionMode(ExceptionMode.PROTECTIVE).intercept { chain ->
            val owner = checkNotNull(chain.thisObject)
            val hidden = runCatching {
                val selected = selectedSlot(owner, isInjector)
                selected != null && owner.field(SLOT_FIELD) as Int != selected
            }.getOrDefault(false)
            if (hidden) {
                chain.proceed(if (isInjector) arrayOf<Any>(0) else arrayOf<Any>(false, false))
            } else {
                chain.proceed()
            }
        }
    }

    private fun selectedSlot(owner: Any, isInjector: Boolean): Int? {
        val isKeyguard = isInjector || owner.field("isKeyguardLayout") as Boolean
        if (!DataSimCarrierSettings.shouldFilter(isKeyguard, notificationBar)) return null
        val controller = owner.field("carrierTextController") ?: return null
        if (controller.field("mAirplane") as Boolean || controller.field("mEmergencyOnly") as Boolean) return null
        val disabled = controller.field("mCardDisable") as BooleanArray
        val slot = dataSlot()
        return slot.takeIf { DataSimCarrierSlots.filter(disabled, it) != null }
    }

    private fun refreshInjector(owner: Any, hidden: Boolean) {
        val view = owner.field("view") as View
        view.visibility = if (hidden) View.GONE else View.VISIBLE
        val slot = owner.field(SLOT_FIELD) as Int
        val hd = checkNotNull(owner.field("hdController"))
        val icon = if (!hidden && hd.call("isVisible", slot) == true) hd.call("getIconId", slot) as Int else 0
        owner.call("updateHDDrawable", icon)
    }

    private companion object {
        const val TAG = "HookHyper-DataSimCarrier"
        const val SLOT_FIELD = "innerCarrierSlotId"
    }
}
