package com.newbieeming.hookhyper.feature.systemui.model

import kotlin.math.round
import kotlin.math.roundToInt

private const val FONT_SIZE_LIMIT = 30f
private const val FONT_SIZE_STEP = 0.5f
private const val FONT_SIZE_INITIAL = 13.5f
private const val SPACING_LIMIT = 5f
private const val SPACING_STEP = 0.1f

internal enum class SystemUiDimension(
    val preferenceKey: String,
    val range: ClosedFloatingPointRange<Float>,
    val step: Float,
    val defaultValue: Float?,
    val initialValue: Float,
) {
    FONT_SIZE(NetworkSpeedSettings.FONT_SIZE, 0f..FONT_SIZE_LIMIT, FONT_SIZE_STEP, null, FONT_SIZE_INITIAL),
    CLOCK_FONT_SIZE(TimeFormatSettings.FONT_SIZE, 0f..FONT_SIZE_LIMIT, FONT_SIZE_STEP, null, FONT_SIZE_INITIAL),
    UNIT_GAP(NetworkSpeedSettings.UNIT_GAP, -SPACING_LIMIT..SPACING_LIMIT, SPACING_STEP, 0f, 0f),
    VERTICAL_OFFSET(NetworkSpeedSettings.VERTICAL_OFFSET, -SPACING_LIMIT..SPACING_LIMIT, SPACING_STEP, 0f, 0f),
    END_PADDING(NetworkSpeedSettings.END_PADDING, -SPACING_LIMIT..SPACING_LIMIT, SPACING_STEP, 0f, 0f),
    ;

    val sliderSteps: Int get() = ((range.endInclusive - range.start) / step).roundToInt() - 1

    fun normalize(value: Float): Float {
        if (!value.isFinite()) return initialValue
        val tickSize = (step * DECIMAL_SCALE).roundToInt()
        val ticks = round(value.coerceIn(range) * DECIMAL_SCALE / tickSize).toInt()
        return (ticks * tickSize).toFloat() / DECIMAL_SCALE
    }

    fun parse(value: String): Float? = value.trim().toFloatOrNull()?.takeIf { it.isFinite() }?.let(::normalize) ?: defaultValue

    private companion object {
        const val DECIMAL_SCALE = 10
    }
}
