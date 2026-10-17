package com.newbieeming.hookhyper.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.newbieeming.hookhyper.ui.app.popToPrevious
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class HookSettingsRouteTest {
    @Test
    fun returnsThroughCategory() {
        val feature = FeatureRoute("com.android.systemui")
        val category = HookCategoryRoute(feature.targetPackageName, "status_bar")
        val detail = HookSettingsRoute(feature.targetPackageName, category.categoryId, "systemui_custom_time_format")
        val stack = mutableListOf<NavKey>(HomeRoute, feature, category, detail)
        stack.popToPrevious()
        assertEquals(category, stack.last())
        stack.popToPrevious()
        assertEquals(feature, stack.last())
        stack.popToPrevious()
        stack.popToPrevious()
        assertEquals(listOf(HomeRoute), stack)
    }

    @Test
    fun restoresNestedDestination() {
        val route = HookSettingsRoute(
            targetPackageName = "com.android.systemui",
            categoryId = "status_bar",
            preferenceKey = "systemui_custom_time_format",
        )
        assertEquals(route, Json.decodeFromString<HookSettingsRoute>(Json.encodeToString(route)))
    }
}
