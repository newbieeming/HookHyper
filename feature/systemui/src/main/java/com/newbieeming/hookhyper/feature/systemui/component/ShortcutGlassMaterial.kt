package com.newbieeming.hookhyper.feature.systemui.component

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.newbieeming.hookhyper.core.hook.HookUtils.field
import com.newbieeming.hookhyper.core.hook.HookUtils.staticField

/** Uses the same native small/thin glass pipeline as the SystemUI Customize Lock screen button. */
internal class ShortcutGlassMaterial(classLoader: ClassLoader) {
    private val blur = classLoader.loadClass("com.miui.systemui.util.MiBlurCompat")
    private val glass = classLoader.loadClass("com.miui.systemui.util.MiGlassCompat")
    private val backgroundMode = blur.getDeclaredMethod("setMiBackgroundBlurModeCompat", Int::class.javaPrimitiveType, View::class.java)
    private val backgroundRadius = blur.getDeclaredMethod("setMiBackgroundBlurRadiusCompat", Int::class.javaPrimitiveType, View::class.java)
    private val passWindow = blur.getDeclaredMethod("setPassWindowBlurEnabledCompat", View::class.java, Boolean::class.javaPrimitiveType)
    private val blurOutline = blur.getDeclaredMethod("setBlurOutlineEnable", View::class.java, Boolean::class.javaPrimitiveType)
    private val glassOutline = blur.getDeclaredMethod("setGlassOutlineEnable", View::class.java, Boolean::class.javaPrimitiveType)
    private val getEnhanceFlag = runCatching {
        View::class.java.getDeclaredMethod("getMiBackgroundBlurEnhanceFlag")
    }.getOrNull()
    private val glassRadius = glass.getDeclaredMethod(
        "setMiGlassBlurRadius",
        View::class.java,
        Int::class.javaPrimitiveType,
        Int::class.javaPrimitiveType,
    )
    private val glassSize = glass.getDeclaredMethod(
        "setMiGlassSdfMaxSizeCompat",
        View::class.java,
        Float::class.javaPrimitiveType,
        Float::class.javaPrimitiveType,
    )
    private val viewMode = blur.getDeclaredMethod("setMiViewBlurModeCompat", Int::class.javaPrimitiveType, View::class.java)
    private val clearBlend = blur.getDeclaredMethod("clearMiBackgroundBlendColorCompat", View::class.java)
    private val materialType = glass.getDeclaredMethod("setMiViewMaterialTypeCompat", Int::class.javaPrimitiveType, View::class.java)
    private val setParameters = glass.getDeclaredMethod("setMiGlassCompat", View::class.java, FloatArray::class.java)
    private val token = requireNotNull(
        classLoader.loadClass("miuix.theme.token.GlassToken").staticField("Glass_Common_Small_Thin"),
    )
    private val parameters = buildParameters(token)
    private val blurRadii = requireNotNull(token.field("blurRadius"))
    private val smallBlurRadius = (blurRadii.field("smallBlurRadius") as Number).toInt()
    private val bigBlurRadius = (blurRadii.field("bigBlurRadius") as Number).toInt()

    fun apply(view: ImageView, context: Context) {
        val radiusId = context.resources.getIdentifier("custom_lockscreen_blur_radius", "dimen", "com.android.systemui")
        check(radiusId != 0) { "SystemUI custom_lockscreen_blur_radius is unavailable" }
        val blurRadius = context.resources.getDimensionPixelSize(radiusId)
        view.background = null
        clearBlend.invoke(null, view)
        blurOutline.invoke(null, view, false)
        glassOutline.invoke(null, view, false)
        passWindow.invoke(null, view, true)
        backgroundRadius.invoke(null, blurRadius, view)
        backgroundMode.invoke(null, 1, view)
        viewMode.invoke(null, 1, view)
        materialType.invoke(null, 1, view)
        setParameters.invoke(null, view, parameters.copyOf())
        glassRadius.invoke(null, view, smallBlurRadius, bigBlurRadius)
    }

    /** Let native glass consume the outline without an additional View clipping pass. */
    fun configureClipping(view: View) {
        val nativeOutlineEnabled = runCatching {
            if (getEnhanceFlag == null) {
                false
            } else {
                glassOutline.invoke(null, view, true)
                (getEnhanceFlag.invoke(view) as Number).toInt() and GLASS_OUTLINE_FLAG != 0
            }
        }.getOrDefault(false)
        if (!nativeOutlineEnabled) glassOutline.invoke(null, view, false)
        view.clipToOutline = !nativeOutlineEnabled
        view.invalidateOutline()
    }

    fun updateSize(view: View) {
        glassSize.invoke(null, view, view.width.toFloat(), view.height.toFloat())
    }

    fun clear(view: ImageView) {
        materialType.invoke(null, 0, view)
        setParameters.invoke(null, view, FloatArray(parameters.size))
        glassRadius.invoke(null, view, 0, 0)
        viewMode.invoke(null, 0, view)
        clearBlend.invoke(null, view)
        backgroundRadius.invoke(null, 0, view)
        backgroundMode.invoke(null, 0, view)
        passWindow.invoke(null, view, false)
        blurOutline.invoke(null, view, false)
        glassOutline.invoke(null, view, false)
    }

    /* Native GlassToken ABI: match KeyguardEditorHelper's conversion to 42 shader slots. */
    @Suppress("MagicNumber")
    private fun buildParameters(token: Any): FloatArray {
        val result = FloatArray(42)
        fun group(name: String): Any = requireNotNull(token.field(name))
        fun scalar(source: Any, name: String, index: Int) {
            result[index] = (source.field(name) as Number).toFloat()
        }
        fun array(source: Any, name: String, offset: Int, count: Int) {
            val values = source.field(name) as? FloatArray ?: return
            values.copyInto(result, destinationOffset = offset, endIndex = minOf(values.size, count))
        }
        val blend = group("blend")
        array(blend, "luminanceValues", 0, 4)
        scalar(blend, "luminanceAmount", 4)
        scalar(blend, "saturation", 5)
        scalar(blend, "brightness", 6)
        scalar(blend, "darker", 7)
        array(blend, "darkerRange", 8, 2)
        val inner = group("inner")
        scalar(inner, "bottom", 10)
        array(inner, "color", 11, 4)
        scalar(inner, "colorWhite", 15)
        scalar(inner, "colorMix", 16)
        scalar(inner, "colorPow", 17)
        result[18] = 1f
        val shape = group("shape")
        scalar(shape, "edge", 19)
        scalar(shape, "edgePow", 20)
        scalar(shape, "thickness", 21)
        scalar(shape, "reflectOffset", 22)
        val reflect = group("reflect")
        scalar(reflect, "lighten", 23)
        scalar(reflect, "strength", 24)
        val light = group("directionalLight")
        array(light, "color", 25, 3)
        scalar(light, "bottom", 28)
        scalar(light, "colorWhite", 29)
        scalar(light, "colorMix", 30)
        scalar(light, "colorPow", 31)
        scalar(group("refract"), "ior", 32)
        val blurBg = group("blurBg")
        scalar(blurBg, "saturation", 33)
        scalar(blurBg, "brightness", 34)
        scalar(blurBg, "burn", 35)
        // Slots 36–39 remain zero, as in the editor button's non-interactive material.
        array(group("mask"), "pressRingStrength", 40, 2)
        return result
    }

    private companion object {
        const val GLASS_OUTLINE_FLAG = 8192
    }
}
