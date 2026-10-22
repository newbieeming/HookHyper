package com.newbieeming.hookhyper.feature.systemui.model

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkSpeedSettingsTest {
    @Test
    fun signedOffset() {
        assertEquals(-0.5f, NetworkSpeedSettings.parseVerticalOffset("-0.5"))
        assertEquals(0.5f, NetworkSpeedSettings.parseVerticalOffset("0.5"))
        assertEquals(0f, NetworkSpeedSettings.parseVerticalOffset("0"))
    }

    @Test
    fun signedRightSpacing() {
        assertEquals(-0.5f, NetworkSpeedSettings.parseEndPadding("-0.5"))
        assertEquals(1.5f, NetworkSpeedSettings.parseEndPadding("1.5"))
        assertEquals(1.4f, NetworkSpeedSettings.parseEndPadding("1.4"))
    }

    @Test
    fun invalidPositionDefaults() {
        listOf("", "invalid", "NaN", "Infinity", "-Infinity").forEach {
            assertEquals(0f, NetworkSpeedSettings.parseVerticalOffset(it))
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
