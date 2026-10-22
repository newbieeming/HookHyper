package com.newbieeming.hookhyper.feature.systemui.model

/** Settings and display rules shared by the form and the SystemUI hook. */
internal object NetworkSpeedSettings {
    const val CUSTOM_FONT_SIZE = "systemui_network_speed_custom_font_size"
    const val FONT_SIZE = "systemui_network_speed_font_size"
    const val SINGLE_LINE = "systemui_network_speed_single_line"
    const val HIDE_UNIT = "systemui_network_speed_hide_unit"
    const val UNIT_GAP = "systemui_network_speed_unit_gap"
    const val VERTICAL_OFFSET = "systemui_network_speed_vertical_offset"
    const val END_PADDING = "systemui_network_speed_end_padding"
    const val DEFAULT_FONT_SIZE = ""
    const val DEFAULT_UNIT_GAP = "0"
    const val DEFAULT_POSITION = "0"

    fun parseFontSize(value: String): Float? = SystemUiDimension.FONT_SIZE.parse(value)

    fun parseUnitGap(value: String): Float = SystemUiDimension.UNIT_GAP.parse(value) ?: 0f

    fun parseVerticalOffset(value: String): Float = SystemUiDimension.VERTICAL_OFFSET.parse(value) ?: 0f

    fun parseEndPadding(value: String): Float = SystemUiDimension.END_PADDING.parse(value) ?: 0f

    fun displayText(value: String, singleLine: Boolean): String = if (singleLine) value.trimEnd() else "$value "

    fun compactUnit(unit: String): String = when {
        unit.trim().equals("KB/s", ignoreCase = true) -> "K"
        unit.trim().equals("MB/s", ignoreCase = true) -> "M"
        else -> unit
    }
}
