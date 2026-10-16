package com.newbieeming.hookhyper.feature.systemui.model

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DataSimCarrierSettingsTest {
    @Test
    fun keyguardAlwaysFiltered() {
        assertTrue(DataSimCarrierSettings.shouldFilter(isKeyguard = true, notificationBar = false))
        assertTrue(DataSimCarrierSettings.shouldFilter(isKeyguard = true, notificationBar = true))
    }

    @Test
    fun shadeFollowsChildSwitch() {
        assertFalse(DataSimCarrierSettings.shouldFilter(isKeyguard = false, notificationBar = false))
        assertTrue(DataSimCarrierSettings.shouldFilter(isKeyguard = false, notificationBar = true))
    }

    @Test
    fun preserveUnfilteredSurface() {
        val original = arrayOf<Any?>(2, "Carrier B", 1)
        assertSame(original, DataSimCarrierSettings.callbackArguments(original, selectedSlot = null, viewSlot = 1))
    }

    @Test
    fun hideOtherSimWithHd() {
        val original = arrayOf<Any?>(2, "Carrier B", 1)
        assertArrayEquals(arrayOf<Any?>(0, "", 1), DataSimCarrierSettings.callbackArguments(original, selectedSlot = 0, viewSlot = 1))
        assertArrayEquals(arrayOf<Any?>(2, "Carrier B", 1), original)
    }

    @Test
    fun keepDataSimLabel() {
        val original = arrayOf<Any?>(2, "Carrier A", 0)
        assertArrayEquals(arrayOf<Any?>(1, "Carrier A", 0), DataSimCarrierSettings.callbackArguments(original, selectedSlot = 0, viewSlot = 0))
        assertTrue(DataSimCarrierSettings.callbackArguments(original, selectedSlot = 0, viewSlot = 1).isEmpty())
    }
}
