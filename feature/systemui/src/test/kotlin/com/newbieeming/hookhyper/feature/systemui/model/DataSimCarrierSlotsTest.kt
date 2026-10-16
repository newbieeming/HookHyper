package com.newbieeming.hookhyper.feature.systemui.model

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DataSimCarrierSlotsTest {
    @Test
    fun switchDataSlot() {
        val disabled = booleanArrayOf(false, false)
        assertArrayEquals(booleanArrayOf(false, true), DataSimCarrierSlots.filter(disabled, 0))
        assertArrayEquals(booleanArrayOf(true, false), DataSimCarrierSlots.filter(disabled, 1))
        assertArrayEquals(booleanArrayOf(false, false), disabled)
    }

    @Test
    fun invalidDataSlot() {
        assertNull(DataSimCarrierSlots.filter(booleanArrayOf(false, false), -1))
        assertNull(DataSimCarrierSlots.filter(booleanArrayOf(false, false), 2))
        assertNull(DataSimCarrierSlots.filter(booleanArrayOf(false, true), 1))
        assertNull(DataSimCarrierSlots.filter(booleanArrayOf(), 0))
    }

    @Test
    fun singleSim() {
        assertArrayEquals(booleanArrayOf(true, false), DataSimCarrierSlots.filter(booleanArrayOf(true, false), 1))
        assertArrayEquals(booleanArrayOf(false), DataSimCarrierSlots.filter(booleanArrayOf(false), 0))
    }
}
