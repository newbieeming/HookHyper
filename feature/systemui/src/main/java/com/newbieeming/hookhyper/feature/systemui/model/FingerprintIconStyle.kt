package com.newbieeming.hookhyper.feature.systemui.model

import androidx.annotation.DrawableRes
import com.newbieeming.hookhyper.feature.systemui.R

/** Resource set used when replacing the under-display fingerprint icon. */
enum class FingerprintIconStyle(
    val id: String,
    @param:DrawableRes val normalResId: Int,
    @param:DrawableRes val lightResId: Int,
    @param:DrawableRes val greyResId: Int,
    @param:DrawableRes val aodResId: Int,
) {
    CIRCLE(
        id = "circle",
        normalResId = R.drawable.circle_finger_image_normal,
        lightResId = R.drawable.circle_finger_image_light,
        greyResId = R.drawable.circle_finger_image_grey,
        aodResId = R.drawable.circle_finger_image_aod,
    ),
    VANILLA(
        id = "vanilla",
        normalResId = R.drawable.vanilla_finger_image_normal,
        lightResId = R.drawable.vanilla_finger_image_light,
        greyResId = R.drawable.vanilla_finger_image_grey,
        aodResId = R.drawable.vanilla_finger_image_aod,
    ),
    ;

    fun replacementFor(hostResourceName: String): Int? = when (hostResourceName) {
        "finger_circle_image_normal" -> normalResId
        "finger_circle_image_light" -> lightResId
        "finger_circle_image_grey", "finger_circle_image_grey_enroll" -> greyResId
        "finger_circle_image_aod" -> aodResId
        else -> null
    }

    companion object {
        const val PREFERENCE_KEY = "systemui_fingerprint_icon_style"

        fun fromId(id: String): FingerprintIconStyle = entries.firstOrNull { it.id == id } ?: CIRCLE
    }
}
