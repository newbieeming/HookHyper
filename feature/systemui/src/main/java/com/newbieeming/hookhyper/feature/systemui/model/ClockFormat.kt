package com.newbieeming.hookhyper.feature.systemui.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** SimpleDateFormat tokens with ISO weekday e, detailed Chinese period aa and double-hour i extensions. */
internal class ClockFormat(pattern: String, private val englishAmPm: Boolean) {
    private val parts = parse(pattern)
    val showsSeconds = parts.any { it.token == "ss" || it.token == "s" }

    @Synchronized
    fun format(time: Long, locale: Locale = Locale.getDefault(), zone: TimeZone = TimeZone.getDefault()): String {
        val calendar = Calendar.getInstance(zone, locale).apply { timeInMillis = time }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return parts.joinToString("") { part ->
            when {
                part.token == null -> part.literal
                part.token == "aa" -> detailedPeriod(hour)
                part.token == "a" -> amPm(hour)
                part.token == "e" -> ((calendar.get(Calendar.DAY_OF_WEEK) + MONDAY_OFFSET) % DAYS_PER_WEEK + 1).toString()
                part.token == "i" -> "${HOURS[(hour + 1) / HOURS_PER_PERIOD % HOURS.size]}时"
                else -> SimpleDateFormat(part.token, locale).apply { timeZone = zone }.format(Date(time))
            }
        }
    }

    private fun amPm(hour: Int): String = if (englishAmPm) {
        if (hour < HALF_DAY_HOURS) "AM" else "PM"
    } else {
        if (hour < HALF_DAY_HOURS) "上午" else "下午"
    }

    private data class Part(val token: String? = null, val literal: String = "")

    private companion object {
        const val HALF_DAY_HOURS = 12
        const val HOURS_PER_PERIOD = 2
        const val MONDAY_OFFSET = 5
        const val DAYS_PER_WEEK = 7
        const val MAX_PATTERN_LENGTH = 256
        val HOURS = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
        val TOKENS = setOf("yyyy", "yy", "MM", "M", "dd", "d", "e", "E", "EE", "EEE", "EEEE", "a", "aa", "HH", "H", "hh", "h", "mm", "m", "ss", "s", "i")

        fun detailedPeriod(hour: Int): String = when (hour) {
            in 0..5 -> "凌晨"
            in 6..8 -> "早上"
            in 9..11 -> "上午"
            HALF_DAY_HOURS -> "中午"
            in 13..16 -> "下午"
            in 17..18 -> "傍晚"
            else -> "晚上"
        }

        fun parse(pattern: String): List<Part> = buildList {
            require(pattern.isNotBlank() && pattern.length <= MAX_PATTERN_LENGTH) { "Invalid clock pattern length" }
            var index = 0
            var quoted = false
            while (index < pattern.length) {
                val char = pattern[index]
                when {
                    char == '\'' -> {
                        if (pattern.getOrNull(index + 1) == '\'') {
                            add(Part(literal = "'"))
                            index++
                        } else {
                            quoted = !quoted
                        }
                        index++
                    }
                    !quoted && (char in 'a'..'z' || char in 'A'..'Z') -> {
                        val start = index
                        index = tokenEnd(pattern, start)
                        val token = pattern.substring(start, index)
                        require(token in TOKENS) { "Unsupported clock token: $token" }
                        add(Part(token = token))
                    }
                    else -> {
                        add(Part(literal = char.toString()))
                        index++
                    }
                }
            }
            require(!quoted) { "Unclosed clock pattern quote" }
        }

        fun tokenEnd(pattern: String, start: Int): Int {
            var end = start + 1
            while (pattern.getOrNull(end) == pattern[start]) end++
            return end
        }
    }
}

internal object TimeFormatSettings {
    const val PATTERN = "systemui_time_format_pattern"
    const val ENGLISH_AM_PM = "systemui_time_format_english_am_pm"
    const val FONT_SIZE = "systemui_time_format_font_size"
    const val NOTIFICATION_BAR = "systemui_time_format_notification_bar"
    const val DEFAULT_PATTERN = "HH:mm a"

    fun fontSize(value: String): Float? = SystemUiDimension.CLOCK_FONT_SIZE.parse(value)
}
