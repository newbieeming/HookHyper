package com.newbieeming.hookhyper.feature.systemui.model

/** Settings and display rules shared by the form and the SystemUI hook. */
internal object NetworkSpeedSettings {
    const val CUSTOM_FONT_SIZE = "systemui_network_speed_custom_font_size"
    const val FONT_SIZE = "systemui_network_speed_font_size"
    const val SINGLE_LINE = "systemui_network_speed_single_line"
    const val HIDE_UNIT = "systemui_network_speed_hide_unit"
    const val UNIT_GAP = "systemui_network_speed_unit_gap"
    const val DEFAULT_FONT_SIZE = "13.5"
    const val DEFAULT_UNIT_GAP = "0"

    fun parseFontSize(value: String): Float? = value.trim().toFloatOrNull()?.takeIf { it.isFinite() && it > 0f }

    fun parseUnitGap(value: String): Float = value.trim().toFloatOrNull()?.takeIf { it.isFinite() && it >= 0f } ?: DEFAULT_UNIT_GAP.toFloat()

    fun dimensionSize(resourceName: String, fontSize: Float): Float? = when (resourceName) {
        "status_bar_network_speed_size", "status_bar_network_speed_unit_size" -> fontSize
        else -> null
    }

    fun resourceUnit(resourceName: String): String? = when (resourceName) {
        "kilobyte_per_second" -> "K"
        "megabyte_per_second" -> "M"
        else -> null
    }

    fun compactUnit(unit: String): String = when {
        unit.trim().equals("KB/s", ignoreCase = true) -> "K"
        unit.trim().equals("MB/s", ignoreCase = true) -> "M"
        else -> unit
    }
}
