package com.newbieeming.hookhyper.feature.systemui.hook

import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.HookUtils.field
import com.newbieeming.hookhyper.core.hook.HookUtils.findField
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSettingsPage
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.component.NetworkSpeedPreferences
import com.newbieeming.hookhyper.feature.systemui.model.NetworkSpeedSettings
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef
import kotlin.math.ceil
import kotlin.math.roundToInt

@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class NetworkSpeedHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {
    override val def = SystemUiHookDef.NETWORK_SPEED
    override val settingsPage = HookSettingsPage(
        titleResId = R.string.systemui_network_speed_title,
        summaryResId = R.string.systemui_network_speed_summary,
    )

    @Composable
    override fun Content() {
        NetworkSpeedPreferences(preferenceKey)
    }

    override fun HookContext.onHook() {
        val preferences = prefs(PreferenceKeys.FILE_NAME)
        val fontSize = if (preferences.getBoolean(NetworkSpeedSettings.CUSTOM_FONT_SIZE)) {
            NetworkSpeedSettings.parseFontSize(
                preferences.getString(NetworkSpeedSettings.FONT_SIZE, NetworkSpeedSettings.DEFAULT_FONT_SIZE),
            )
        } else {
            null
        }
        val singleLine = preferences.getBoolean(NetworkSpeedSettings.SINGLE_LINE)
        val hideUnit = preferences.getBoolean(NetworkSpeedSettings.HIDE_UNIT)
        val unitGap = NetworkSpeedSettings.parseUnitGap(
            preferences.getString(NetworkSpeedSettings.UNIT_GAP, NetworkSpeedSettings.DEFAULT_UNIT_GAP),
        )
        val verticalOffset = NetworkSpeedSettings.parsePosition(
            preferences.getString(NetworkSpeedSettings.VERTICAL_OFFSET, NetworkSpeedSettings.DEFAULT_POSITION),
            allowNegative = true,
        )
        val endPadding = NetworkSpeedSettings.parsePosition(
            preferences.getString(NetworkSpeedSettings.END_PADDING, NetworkSpeedSettings.DEFAULT_POSITION),
            allowNegative = false,
        )
        hookSafely("NetworkSpeedView") {
            val viewClass = NETWORK_SPEED_VIEW.toClass()
            val refreshMethods = viewClass.declaredMethods.filter {
                it.parameterCount == 0 && (it.name == "onFinishInflate" || it.name.startsWith("updateResources"))
            }
            check(refreshMethods.isNotEmpty()) { "NetworkSpeedView refresh methods not found" }
            refreshMethods.forEach { method ->
                hookSafely(method.name) {
                    xposed.hook(method).intercept { chain ->
                        val result = chain.proceed()
                        updateSafely {
                            applyAppearance(chain.thisObject as ViewGroup, fontSize, singleLine, unitGap)
                            applyPosition(chain.thisObject as ViewGroup, verticalOffset, endPadding)
                        }
                        result
                    }
                }
            }
            if (singleLine || hideUnit) hookSpeedUpdates(viewClass, singleLine, hideUnit)
            if (singleLine) hookSpeedWidth(viewClass, hideUnit)
        }
    }

    private fun HookContext.hookSpeedUpdates(viewClass: Class<*>, singleLine: Boolean, hideUnit: Boolean) {
        viewClass.declaredMethods.filter {
            it.parameterCount == 0 && it.name.startsWith("updateNetworkSpeed")
        }.forEach { method ->
            hookSafely(method.name) {
                xposed.hook(method).intercept { chain ->
                    // The stock method appends spaces before each comparison. Write the final text once.
                    runCatching { updateSpeedText(chain.thisObject as ViewGroup, singleLine, hideUnit) }
                        .onFailure { Log.e(TAG, "Unable to update network speed text", it) }
                        .fold(onSuccess = { null }, onFailure = { chain.proceed() })
                }
            }
        }
    }

    private fun updateSpeedText(view: ViewGroup, singleLine: Boolean, hideUnit: Boolean) {
        val number = checkNotNull(view.namedView<TextView>(NUMBER_VIEW_ID))
        val unit = checkNotNull(view.namedView<TextView>(UNIT_VIEW_ID))
        val numberText = view.field("mNetworkSpeedNumber").toString()
        val rawUnit = view.field("mNetworkSpeedUnit").toString()
        val unitText = if (hideUnit) NetworkSpeedSettings.compactUnit(rawUnit) else rawUnit
        number.setTextIfChanged(NetworkSpeedSettings.displayText(numberText, singleLine))
        unit.setTextIfChanged(NetworkSpeedSettings.displayText(unitText, singleLine))
        val description = numberText + unitText
        if (view.contentDescription?.toString() != description) view.contentDescription = description
    }

    private fun applyPosition(view: ViewGroup, verticalOffset: Float, endPadding: Float) {
        // Leave the outer view's translation to StatusBarIconAnimHelper.
        view.namedView<View>(CONTAINER_VIEW_ID)?.translationY = verticalOffset * view.resources.displayMetrics.density
        view.setPaddingRelative(view.paddingStart, view.paddingTop, view.dpToPixels(endPadding), view.paddingBottom)
    }

    private fun applyAppearance(view: ViewGroup, fontSize: Float?, singleLine: Boolean, unitGap: Float) {
        val number = view.namedView<TextView>(NUMBER_VIEW_ID) ?: return
        val unit = view.namedView<TextView>(UNIT_VIEW_ID) ?: return
        // Apply locally after the system TextAppearance; avoid intercepting process-wide Resources.
        if (fontSize != null) {
            number.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
            unit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
        }
        if (!singleLine) return
        makeHorizontal(view, number, unit, unitGap)
        listOf(number, unit).forEach {
            it.translationY = 0f
            it.gravity = Gravity.CENTER_VERTICAL
            it.typeface = Typeface.create(it.typeface, Typeface.NORMAL)
            it.setSingleLine(true)
            it.setTextIfChanged(it.text.toString().trimEnd())
        }
    }

    private fun makeHorizontal(view: ViewGroup, number: TextView, unit: TextView, unitGap: Float) {
        val container = view.namedView<ViewGroup>(CONTAINER_VIEW_ID) ?: return
        val parent = container.parent as? ViewGroup ?: return
        if (container !is LinearLayout) {
            // Validate the host field before reparenting. SystemUI uses it for icon visibility.
            val containerField = view.javaClass.findField("mContainer")
            check(containerField.type.isAssignableFrom(LinearLayout::class.java)) { "Unsupported mContainer type" }
            val replacement = LinearLayout(view.context).apply {
                id = container.id
                layoutParams = container.layoutParams
                visibility = container.visibility
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val index = parent.indexOfChild(container)
            container.removeView(number)
            container.removeView(unit)
            replacement.addView(number)
            replacement.addView(unit)
            parent.removeViewAt(index)
            parent.addView(replacement, index)
            containerField.set(view, replacement)
        } else {
            container.orientation = LinearLayout.HORIZONTAL
            container.gravity = Gravity.CENTER_VERTICAL
        }
        number.layoutParams = horizontalTextParams()
        unit.layoutParams = horizontalTextParams().apply { marginStart = view.dpToPixels(unitGap) }
        (view as? LinearLayout)?.orientation = LinearLayout.HORIZONTAL
    }

    private fun HookContext.hookSpeedWidth(viewClass: Class<*>, hideUnit: Boolean) {
        hookSafely("NetworkSpeedView.getNetworkSpeedWidth") {
            val method = viewClass.getDeclaredMethod("getNetworkSpeedWidth")
            xposed.hook(method).intercept { chain ->
                val original = chain.proceed()
                val view = chain.thisObject as ViewGroup
                // The stock fallback takes max(number, unit) for the stacked layout; use their sum.
                if (view.width > view.paddingStart + view.paddingEnd) {
                    original
                } else {
                    runCatching { horizontalWidth(view, hideUnit) }.getOrDefault(original)
                }
            }
        }
    }

    private fun horizontalWidth(view: ViewGroup, hideUnit: Boolean): Int {
        val number = checkNotNull(view.namedView<TextView>(NUMBER_VIEW_ID))
        val unit = checkNotNull(view.namedView<TextView>(UNIT_VIEW_ID))
        val numberWidth = number.paint.measureText(number.text.toString().ifEmpty { "0.00" }) +
            number.compoundPaddingLeft + number.compoundPaddingRight
        val unitWidth = unit.paint.measureText(unit.text.toString().ifEmpty { if (hideUnit) "M" else "MB/s" }) +
            unit.compoundPaddingLeft + unit.compoundPaddingRight
        val container = checkNotNull(view.namedView<View>(CONTAINER_VIEW_ID))
        val margin = (unit.layoutParams as? ViewGroup.MarginLayoutParams)?.marginStart ?: 0
        return (
            ceil(numberWidth + unitWidth).toInt() + margin + container.paddingStart + container.paddingEnd +
                view.paddingStart + view.paddingEnd
            ).coerceAtLeast(0)
    }

    private inline fun <reified T : View> View.namedView(name: String): T? {
        val id = resources.getIdentifier(name, "id", SystemUiFeatureEntry.PACKAGE_NAME)
        return if (id == 0) null else findViewById<View>(id) as? T
    }

    private fun TextView.setTextIfChanged(value: String) {
        if (text.toString() != value) text = value
    }

    private fun horizontalTextParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    ).apply { gravity = Gravity.CENTER_VERTICAL }

    private fun View.dpToPixels(value: Float): Int = (value * resources.displayMetrics.density).roundToInt()

    private inline fun updateSafely(block: () -> Unit) {
        runCatching(block).onFailure { Log.e(TAG, "Unable to update network speed view", it) }
    }

    private companion object {
        const val TAG = "HookHyper-NetworkSpeed"
        const val NETWORK_SPEED_VIEW = "com.android.systemui.statusbar.views.NetworkSpeedView"
        const val NUMBER_VIEW_ID = "network_speed_number"
        const val UNIT_VIEW_ID = "network_speed_unit"
        const val CONTAINER_VIEW_ID = "network_speed_container"
    }
}
