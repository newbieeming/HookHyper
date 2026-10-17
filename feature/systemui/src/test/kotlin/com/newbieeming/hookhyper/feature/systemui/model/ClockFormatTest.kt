package com.newbieeming.hookhyper.feature.systemui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ClockFormatTest {
    private val zone = TimeZone.getTimeZone("Asia/Shanghai")

    private fun time(hour: Int, minute: Int = 5, second: Int = 9): Long = Calendar.getInstance(zone).apply {
        clear()
        set(2026, Calendar.OCTOBER, 5, hour, minute, second)
    }.timeInMillis

    private fun render(pattern: String, hour: Int, english: Boolean = false): String = ClockFormat(pattern, english).format(time(hour), Locale.SIMPLIFIED_CHINESE, zone)

    @Test
    fun requestedTokensAndPadding() {
        assertEquals("2026 26 10 10 05 5 1 周一 上午 凌晨 00 0 12 12 05 09 子时", render("yyyy yy MM M dd d e E a aa HH H hh h mm ss i", 0))
        assertEquals("13 13 01 1 下午", render("HH H hh h a", 13))
    }

    @Test
    fun noonMidnightAndEnglishSwitch() {
        assertEquals("AM 凌晨 12", render("a aa hh", 0, true))
        assertEquals("PM 中午 12", render("a aa hh", 12, true))
        assertEquals("上午", render("a", 11))
        assertEquals("下午", render("a", 12))
        assertEquals("AM", render("a", 0, true))
    }

    @Test
    fun chinesePeriodsIgnoreSwitch() {
        val expected = listOf("凌晨", "凌晨", "凌晨", "凌晨", "凌晨", "凌晨", "早上", "早上", "早上", "上午", "上午", "上午", "中午", "下午", "下午", "下午", "下午", "傍晚", "傍晚", "晚上", "晚上", "晚上", "晚上", "晚上")
        expected.forEachIndexed { hour, period ->
            assertEquals(period, render("aa", hour))
            assertEquals(period, render("aa", hour, true))
        }
    }

    @Test
    fun doubleHourBoundaries() {
        val expected = listOf("子时", "丑时", "丑时", "寅时", "寅时", "卯时", "卯时", "辰时", "辰时", "巳时", "巳时", "午时", "午时", "未时", "未时", "申时", "申时", "酉时", "酉时", "戌时", "戌时", "亥时", "亥时", "子时")
        expected.forEachIndexed { hour, value -> assertEquals(value, render("i", hour)) }
    }

    @Test
    fun weekdayAndTimezone() {
        val sunday = time(0) - 24 * 60 * 60 * 1000
        assertEquals("7", ClockFormat("e", false).format(sunday, Locale.US, zone))
        assertEquals("16", ClockFormat("HH", false).format(time(0), Locale.US, TimeZone.getTimeZone("UTC")))
    }

    @Test
    fun quotedTokensStayLiteral() {
        assertEquals("a i ss ' 上午", render("'a i ss' '' a", 0))
        assertFalse(ClockFormat("HH:mm 'ss'", false).showsSeconds)
        assertTrue(ClockFormat("HH:mm:ss", false).showsSeconds)
    }

    @Test
    fun invalidPatternsAndSizes() {
        listOf("", " ", "HH:mm q", "'HH:mm", "aaa", "HHH").forEach {
            assertTrue(runCatching { ClockFormat(it, false) }.isFailure)
        }
        listOf("", "NaN", "Infinity", "-1", "0", "101").forEach {
            assertNull(TimeFormatSettings.fontSize(it))
        }
        assertEquals(13.5f, TimeFormatSettings.fontSize("13.5"))
    }
}
