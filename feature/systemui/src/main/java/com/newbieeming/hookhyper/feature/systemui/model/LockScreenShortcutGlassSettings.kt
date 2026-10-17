package com.newbieeming.hookhyper.feature.systemui.model

internal data class LockScreenShortcutGlassSettings(
    val widthDp: Float,
    val heightDp: Float,
    val cornerRadiusDp: Float,
) {
    companion object {
        const val WIDTH_KEY = "systemui_lock_screen_shortcut_glass_width"
        const val HEIGHT_KEY = "systemui_lock_screen_shortcut_glass_height"
        const val RADIUS_KEY = "systemui_lock_screen_shortcut_glass_radius"
        const val DEFAULT_SIZE = 56f
        const val DEFAULT_RADIUS = 28f
        const val MIN_SIZE = 32f
        const val MAX_SIZE = 96f

        fun from(width: String, height: String, radius: String): LockScreenShortcutGlassSettings {
            val widthDp = dimension(width, DEFAULT_SIZE, MIN_SIZE, MAX_SIZE)
            val heightDp = dimension(height, DEFAULT_SIZE, MIN_SIZE, MAX_SIZE)
            val radiusDp = dimension(radius, DEFAULT_RADIUS, 0f, minOf(widthDp, heightDp) / 2f)
            return LockScreenShortcutGlassSettings(widthDp, heightDp, radiusDp)
        }

        private fun dimension(text: String, default: Float, min: Float, max: Float): Float =
            (text.trim().toFloatOrNull()?.takeIf { it.isFinite() } ?: default).coerceIn(min, max)
    }
}
