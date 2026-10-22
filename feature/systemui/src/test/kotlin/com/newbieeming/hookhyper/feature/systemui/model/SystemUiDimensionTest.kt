package com.newbieeming.hookhyper.feature.systemui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@Suppress("MagicNumber")
class SystemUiDimensionTest {
    @Test
    fun fontDefaultDiffersFromZero() {
        assertNull(NetworkSpeedSettings.parseFontSize(""))
        assertNull(NetworkSpeedSettings.parseFontSize("NaN"))
        assertEquals(0f, NetworkSpeedSettings.parseFontSize("0"))
        assertEquals(30f, NetworkSpeedSettings.parseFontSize("100"))
        assertEquals(13.5f, NetworkSpeedSettings.parseFontSize("13.6"))
    }

    @Test
    fun acceptsNegativeUnitSpacing() {
        assertEquals(-2.3f, NetworkSpeedSettings.parseUnitGap("-2.3"))
        assertEquals(-5f, NetworkSpeedSettings.parseUnitGap("-20"))
        assertEquals(5f, NetworkSpeedSettings.parseUnitGap("20"))
    }

    @Test
    fun positionUsesTenths() {
        assertEquals(-0.1f, NetworkSpeedSettings.parseVerticalOffset("-0.1"))
        assertEquals(0.3f, NetworkSpeedSettings.parseEndPadding("0.26"))
        assertEquals(5f, NetworkSpeedSettings.parseVerticalOffset("3.4e38"))
        assertEquals(-0.1f, NetworkSpeedSettings.parseEndPadding("-0.1"))
        assertEquals(-5f, NetworkSpeedSettings.parseEndPadding("-10"))
    }

    @Test
    fun everyTickRoundTrips() {
        SystemUiDimension.entries.forEach { dimension ->
            val intervals = dimension.sliderSteps + 1
            for (tick in 0..intervals) {
                val value = dimension.normalize(dimension.range.start + tick * dimension.step)
                assertEquals(value, dimension.parse(value.toString()))
            }
            assertEquals(dimension.range.start, dimension.normalize(dimension.range.start))
            assertEquals(dimension.range.endInclusive, dimension.normalize(dimension.range.endInclusive))
        }
        assertEquals(59, SystemUiDimension.FONT_SIZE.sliderSteps)
        assertEquals(59, SystemUiDimension.CLOCK_FONT_SIZE.sliderSteps)
        assertEquals(99, SystemUiDimension.UNIT_GAP.sliderSteps)
        assertEquals(99, SystemUiDimension.VERTICAL_OFFSET.sliderSteps)
        assertEquals(99, SystemUiDimension.END_PADDING.sliderSteps)
    }
}
