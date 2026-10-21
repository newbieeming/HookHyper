package com.newbieeming.hookhyper.feature.systemui.model

internal object WifiIconRules {
    fun withoutPlus(
        icon: Int,
        plusIcons: IntArray,
        normalIcons: IntArray,
        hotspotIcons: IntArray,
        metered: Boolean,
    ): Int? {
        val level = plusIcons.indexOf(icon)
        if (level < 0) return null
        return (if (metered) hotspotIcons else normalIcons).getOrNull(level)
    }
}
