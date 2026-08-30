package com.newbieeming.hookhyper.feature.settings.model

import com.newbieeming.hookhyper.core.ui.component.HookCategory
import com.newbieeming.hookhyper.feature.settings.R

enum class SettingsCategory(
    override val id: String,
    override val order: Int,
    override val titleResId: Int,
) : HookCategory {
    DEVICE(id = "device", order = 0, titleResId = R.string.category_device),
}
