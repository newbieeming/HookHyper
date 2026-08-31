package com.newbieeming.hookhyper.feature.systemui.model

import com.newbieeming.hookhyper.core.ui.component.HookCategory
import com.newbieeming.hookhyper.feature.systemui.R

enum class SystemUiCategory(
    override val id: String,
    override val order: Int,
    override val titleResId: Int,
) : HookCategory {
    LOCK_SCREEN(id = "lock_screen", order = 0, titleResId = R.string.category_lock_screen),
    STATUS_BAR(id = "status_bar", order = 1, titleResId = R.string.category_status_bar),
    NOTIFICATION_BAR(id = "notification_bar", order = 2, titleResId = R.string.category_notification_bar),
    SUPER_ISLAND(id = "super_island", order = 3, titleResId = R.string.category_super_island),
}
