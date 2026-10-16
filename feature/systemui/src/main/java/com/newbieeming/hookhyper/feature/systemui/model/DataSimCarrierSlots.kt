package com.newbieeming.hookhyper.feature.systemui.model

internal object DataSimCarrierSlots {
    fun filter(disabled: BooleanArray, dataSlot: Int): BooleanArray? {
        if (dataSlot !in disabled.indices || disabled[dataSlot]) return null
        return BooleanArray(disabled.size) { it != dataSlot }
    }
}
