package com.newbieeming.hookhyper.feature.systemui.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockScopeTest {
    @Test
    fun statusBarAlwaysApplies() {
        assertTrue(ClockScope.shouldApply("clock", listOf("CollapsedStatusBarFragment", "status_bar"), false))
        assertTrue(ClockScope.shouldApply("status_bar_clock", emptyList(), true))
    }

    @Test
    fun notificationRequiresSwitch() {
        listOf(
            "big_time" to listOf("MiuiClock"),
            "horizontal_time" to listOf("MiuiNotificationHeaderView"),
            "clock" to listOf("QuickStatusBarHeader"),
            "clock" to listOf("notification_panel_header"),
            "clock" to listOf("ControlCenterHeaderView"),
        ).forEach { (id, hierarchy) ->
            assertFalse(ClockScope.shouldApply(id, hierarchy, false))
            assertTrue(ClockScope.shouldApply(id, hierarchy, true))
        }
    }

    @Test
    fun excludeUnrelatedViews() {
        assertFalse(ClockScope.shouldApply("clock", listOf("KeyguardStatusBarView"), true))
        assertFalse(ClockScope.shouldApply("big_time", listOf("LockScreenClock", "ControlCenter"), true))
        assertFalse(ClockScope.shouldApply("date", listOf("QuickStatusBarHeader"), true))
    }

    @Test
    fun dateViewsStayDates() {
        assertFalse(ClockScope.shouldApply("date_time", listOf("MiuiNotificationHeaderView"), true, 4))
        assertFalse(ClockScope.shouldApply("date_time", listOf("MiuiQSHeaderView"), true))
        listOf(1, 3, 4).forEach { mode ->
            assertFalse(ClockScope.shouldApply("clock", listOf("MiuiQSHeaderView"), true, mode))
        }
        assertTrue(ClockScope.shouldApply("big_time", listOf("MiuiNotificationHeaderView"), true, 0))
        assertTrue(ClockScope.shouldApply("horizontal_time", listOf("MiuiNotificationHeaderView"), true, 2))
    }
}
