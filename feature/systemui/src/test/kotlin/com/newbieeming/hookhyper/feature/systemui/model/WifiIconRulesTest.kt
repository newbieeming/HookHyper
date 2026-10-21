package com.newbieeming.hookhyper.feature.systemui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@Suppress("MagicNumber")
class WifiIconRulesTest {
    private val plusIcons = intArrayOf(100, 101, 102, 103)
    private val normalIcons = intArrayOf(200, 201, 202, 203)
    private val hotspotIcons = intArrayOf(300, 301, 302, 303)

    @Test
    fun preservesSignalLevel() {
        plusIcons.indices.forEach { level ->
            assertEquals(normalIcons[level], replace(plusIcons[level], metered = false))
        }
    }

    @Test
    fun meteredUsesHotspotFallback() {
        plusIcons.indices.forEach { level ->
            assertEquals(hotspotIcons[level], replace(plusIcons[level], metered = true))
        }
    }

    @Test
    fun leavesOtherIconsUntouched() {
        (normalIcons + hotspotIcons + intArrayOf(0, -1)).forEach {
            assertNull(replace(it, metered = false))
        }
    }

    @Test
    fun missingLevelFallsBack() {
        assertNull(WifiIconRules.withoutPlus(plusIcons.last(), plusIcons, intArrayOf(), hotspotIcons, metered = false))
    }

    private fun replace(icon: Int, metered: Boolean): Int? = WifiIconRules.withoutPlus(icon, plusIcons, normalIcons, hotspotIcons, metered)
}
