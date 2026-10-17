package com.newbieeming.hookhyper.feature.systemui.model

/** Restrict shared Clock classes to the requested surface, including generic clock IDs in headers. */
internal object ClockScope {
    private const val TIME_MODE = 0
    private const val DATE_TIME_MODE = 2

    private val statusBarIds = setOf("clock", "status_bar_clock", "clock_left", "clock_center", "clock_right")
    private val notificationIds = setOf("big_time", "horizontal_time", "header_clock", "qs_clock", "notification_clock", "cc_clock")
    private val notificationMarkers = listOf(
        "quickstatusbarheader", "qsheader", "qs_header", "notificationheader", "notification_header",
        "notification_panel_header", "notification_shade_header", "controlcenter", "control_center",
    )

    fun shouldApply(clockId: String, hierarchy: List<String>, notificationBar: Boolean, clockMode: Int? = null): Boolean {
        // MiuiClock modes 1/3/4 render dates, including the misleadingly named date_time view.
        if (clockMode != null && clockMode !in setOf(TIME_MODE, DATE_TIME_MODE)) return false
        val names = hierarchy.map { it.lowercase() }
        if (names.any { "keyguard" in it || "lockscreen" in it || "lock_screen" in it }) return false
        val isNotification = clockId in notificationIds || names.any { name -> notificationMarkers.any { it in name } }
        return when {
            clockId !in statusBarIds && clockId !in notificationIds -> false
            isNotification -> notificationBar
            else -> clockId in statusBarIds
        }
    }
}
