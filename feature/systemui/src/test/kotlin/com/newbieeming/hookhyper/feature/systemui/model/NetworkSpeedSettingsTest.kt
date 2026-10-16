package com.newbieeming.hookhyper.feature.systemui.model

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkSpeedSettingsTest {
    @Test
    fun signedOffset() {
        assertEquals(-0.5f, NetworkSpeedSettings.parsePosition("-0.5", allowNegative = true))
        assertEquals(0.5f, NetworkSpeedSettings.parsePosition("0.5", allowNegative = true))
        assertEquals(0f, NetworkSpeedSettings.parsePosition("0", allowNegative = true))
    }

    @Test
    fun nonNegativeSpacing() {
        assertEquals(0f, NetworkSpeedSettings.parsePosition("-0.5", allowNegative = false))
        assertEquals(1.5f, NetworkSpeedSettings.parsePosition("1.5", allowNegative = false))
        assertEquals(1.5f, NetworkSpeedSettings.parsePosition("1.4", allowNegative = false))
    }

    @Test
    fun invalidPositionDefaults() {
        listOf("", "invalid", "NaN", "Infinity", "-Infinity", "3.4e38").forEach {
            assertEquals(0f, NetworkSpeedSettings.parsePosition(it, allowNegative = true))
        }
    }

    @Test
    fun finalTextHasStableSpacing() {
        assertEquals("12.3", NetworkSpeedSettings.displayText("12.3 ", singleLine = true))
        assertEquals("12.3 ", NetworkSpeedSettings.displayText("12.3", singleLine = false))
        assertEquals("K", NetworkSpeedSettings.displayText(NetworkSpeedSettings.compactUnit("KB/s"), singleLine = true))
        assertEquals("M ", NetworkSpeedSettings.displayText(NetworkSpeedSettings.compactUnit("MB/s"), singleLine = false))
        assertEquals("B/s", NetworkSpeedSettings.compactUnit("B/s"))
    }
}
