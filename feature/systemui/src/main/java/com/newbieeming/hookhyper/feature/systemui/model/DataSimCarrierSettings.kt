package com.newbieeming.hookhyper.feature.systemui.model

internal object DataSimCarrierSettings {
    const val NOTIFICATION_BAR = "systemui_data_sim_carrier_notification_bar"

    fun shouldFilter(isKeyguard: Boolean, notificationBar: Boolean): Boolean = isKeyguard || notificationBar

    fun callbackArguments(arguments: Array<Any?>, selectedSlot: Int?, viewSlot: Int): Array<Any?> {
        if (selectedSlot == null) return arguments
        // A listener receives every slot; ignore unrelated callbacks for this view.
        val slot = arguments[2] as Int
        if (slot != viewSlot) return emptyArray()
        val hidden = slot != selectedSlot
        return arguments.copyOf().apply {
            this[0] = if (hidden) 0 else 1
            if (hidden) this[1] = ""
        }
    }
}
