package com.newbieeming.hookhyper.feature.systemui.hook

import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.compose.runtime.Composable
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.HookUtils.field
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSettingsPage
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.component.TimeFormatPreferences
import com.newbieeming.hookhyper.feature.systemui.model.ClockFormat
import com.newbieeming.hookhyper.feature.systemui.model.ClockScope
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef
import com.newbieeming.hookhyper.feature.systemui.model.TimeFormatSettings
import java.util.WeakHashMap

@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class TimeFormatHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {
    override val def = SystemUiHookDef.CUSTOM_TIME_FORMAT
    override val settingsPage = HookSettingsPage(
        titleResId = R.string.systemui_custom_time_format_title,
        summaryResId = R.string.systemui_custom_time_format_summary,
    )

    @Composable
    override fun Content() = TimeFormatPreferences(preferenceKey)

    override fun HookContext.onHook() {
        val preferences = prefs(PreferenceKeys.FILE_NAME)
        val legacyPrefix = preferences.getBoolean(SystemUiHookDef.TIME_FORMAT_AA_PREFIX.preferenceKey)
        val pattern = preferences.getString(TimeFormatSettings.PATTERN, if (legacyPrefix) "a HH:mm" else TimeFormatSettings.DEFAULT_PATTERN)
        val formatter = runCatching {
            ClockFormat(pattern, preferences.getBoolean(TimeFormatSettings.ENGLISH_AM_PM))
        }.getOrElse {
            Log.e(TAG, "Invalid time format; using default", it)
            ClockFormat(TimeFormatSettings.DEFAULT_PATTERN, preferences.getBoolean(TimeFormatSettings.ENGLISH_AM_PM))
        }
        val fontSize = TimeFormatSettings.fontSize(preferences.getString(TimeFormatSettings.FONT_SIZE))
        val notificationBar = preferences.getBoolean(TimeFormatSettings.NOTIFICATION_BAR)
        val installed = WeakHashMap<TextView, Unit>()
        CLOCK_CLASSES.forEach { className ->
            hookSafely(className) {
                val clockClass = className.toClass()
                check(TextView::class.java.isAssignableFrom(clockClass)) { "Clock is not a TextView" }
                clockClass.declaredConstructors.forEach { constructor ->
                    xposed.hook(constructor).intercept { chain ->
                        val result = chain.proceed()
                        installUpdates(chain.thisObject as TextView, installed, formatter, fontSize, notificationBar)
                        result
                    }
                }
                val methods = clockClass.declaredMethods.filter {
                    it.name in setOf("updateTime", "updateClock", "onDensityOrFontScaleChanged", "updateResources")
                }
                check(methods.isNotEmpty()) { "Clock refresh methods not found" }
                methods.forEach { method ->
                    hookSafely("$className.${method.name}") {
                        xposed.hook(method).intercept { chain ->
                            val result = chain.proceed()
                            val view = chain.thisObject as TextView
                            installUpdates(view, installed, formatter, fontSize, notificationBar)
                            updateClock(view, formatter, fontSize, notificationBar)
                            result
                        }
                    }
                }
            }
        }
    }

    private fun installUpdates(view: TextView, installed: WeakHashMap<TextView, Unit>, formatter: ClockFormat, fontSize: Float?, notificationBar: Boolean) {
        synchronized(installed) {
            if (installed.put(view, Unit) != null) return
        }
        // The view owns its listener. Only attached clocks queue a tick; detaching removes it.
        val ticker = object : Runnable {
            override fun run() {
                if (!view.isAttachedToWindow || !isTargetClock(view, notificationBar)) return
                if (view.isShown) updateClock(view, formatter, fontSize, notificationBar)
                view.postDelayed(this, 1000L - System.currentTimeMillis() % 1000L)
            }
        }
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                updateClock(view, formatter, fontSize, notificationBar)
                if (formatter.showsSeconds && isTargetClock(view, notificationBar)) {
                    view.removeCallbacks(ticker)
                    view.post(ticker)
                }
            }

            override fun onViewDetachedFromWindow(v: View) {
                view.removeCallbacks(ticker)
            }
        })
        if (view.isAttachedToWindow && formatter.showsSeconds && isTargetClock(view, notificationBar)) view.post(ticker)
    }

    private fun updateClock(view: TextView, formatter: ClockFormat, fontSize: Float?, notificationBar: Boolean) {
        if (!isTargetClock(view, notificationBar)) return
        runCatching {
            val text = formatter.format(System.currentTimeMillis())
            if (view.text.toString() != text) view.text = text
            view.contentDescription = text
            if (fontSize != null && isTargetClock(view, notificationBar = false)) {
                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
            }
        }.onFailure { Log.e(TAG, "Unable to update clock", it) }
    }

    private fun isTargetClock(view: TextView, notificationBar: Boolean): Boolean = runCatching {
        val clockId = view.resources.getResourceEntryName(view.id)
        val hierarchy = buildList {
            var current: View? = view
            while (current != null) {
                add(current.javaClass.simpleName)
                val node = current
                runCatching { node.resources.getResourceEntryName(node.id) }.getOrNull()?.let { add(it) }
                current = current.parent as? View
            }
        }
        val clockMode = runCatching { view.field("mClockMode") as? Int }.getOrNull()
        ClockScope.shouldApply(clockId, hierarchy, notificationBar, clockMode)
    }.getOrDefault(false)

    private companion object {
        const val TAG = "HookHyper-TimeFormat"
        const val MIUI_CLOCK = "com.android.systemui.statusbar.views.MiuiClock"
        val CLOCK_CLASSES = listOf(
            MIUI_CLOCK,
            "com.android.systemui.statusbar.policy.Clock",
        )
    }
}
