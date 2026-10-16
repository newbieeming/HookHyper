package com.newbieeming.hookhyper.feature.systemui.model

import kotlin.math.round

/** Settings and display rules shared by the form and the SystemUI hook. */
internal object NetworkSpeedSettings {
    const val CUSTOM_FONT_SIZE = "systemui_network_speed_custom_font_size"
    const val FONT_SIZE = "systemui_network_speed_font_size"
    const val SINGLE_LINE = "systemui_network_speed_single_line"
    const val HIDE_UNIT = "systemui_network_speed_hide_unit"
    const val UNIT_GAP = "systemui_network_speed_unit_gap"
    const val VERTICAL_OFFSET = "systemui_network_speed_vertical_offset"
    const val END_PADDING = "systemui_network_speed_end_padding"
    const val DEFAULT_FONT_SIZE = "13.5"
    const val DEFAULT_UNIT_GAP = "0"
    const val DEFAULT_POSITION = "0"
    const val POSITION_STEP = 0.5f

    fun parseFontSize(value: String): Float? = value.trim().toFloatOrNull()?.takeIf { it.isFinite() && it > 0f }

    fun parseUnitGap(value: String): Float = value.trim().toFloatOrNull()?.takeIf { it.isFinite() && it >= 0f } ?: DEFAULT_UNIT_GAP.toFloat()

    fun parsePosition(value: String, allowNegative: Boolean): Float {
        val parsed = value.trim().toFloatOrNull()?.takeIf { it.isFinite() && (allowNegative || it >= 0f) } ?: return 0f
        return (round(parsed / POSITION_STEP) * POSITION_STEP).takeIf { it.isFinite() } ?: 0f
    }

    fun displayText(value: String, singleLine: Boolean): String = if (singleLine) value.trimEnd() else "$value "

    fun compactUnit(unit: String): String = when {
        unit.trim().equals("KB/s", ignoreCase = true) -> "K"
        unit.trim().equals("MB/s", ignoreCase = true) -> "M"
        else -> unit
    }
}
