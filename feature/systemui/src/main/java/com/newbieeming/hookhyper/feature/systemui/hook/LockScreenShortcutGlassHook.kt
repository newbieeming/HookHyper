package com.newbieeming.hookhyper.feature.systemui.hook

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.HookUtils.field
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSettingsPage
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.core.ui.component.PreferenceTextField
import com.newbieeming.hookhyper.core.ui.component.PreferenceTextFieldGroup
import com.newbieeming.hookhyper.core.ui.component.SettingsPreferenceGroup
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.component.ShortcutGlassBackground
import com.newbieeming.hookhyper.feature.systemui.component.ShortcutGlassMaterial
import com.newbieeming.hookhyper.feature.systemui.model.LockScreenShortcutGlassSettings
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef
import java.util.Collections
import java.util.WeakHashMap

@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class LockScreenShortcutGlassHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {
    override val def = SystemUiHookDef.LOCK_SCREEN_SHORTCUT_GLASS
    override val settingsPage = HookSettingsPage(
        titleResId = R.string.systemui_shortcut_glass_title,
        summaryResId = R.string.systemui_shortcut_glass_summary,
    )

    @Composable
    override fun Content() {
        val repository = LocalPreferencesRepository.current
        var enabled by remember { mutableStateOf(repository.getBoolean(preferenceKey)) }
        SettingsPreferenceGroup {
            HookSwitchPreference(
                preferenceKey = preferenceKey,
                title = stringResource(R.string.systemui_shortcut_glass_title),
                summary = stringResource(R.string.systemui_shortcut_glass_summary),
                onCheckedChange = { enabled = it },
            )
            AnimatedVisibility(enabled) {
                Column {
                    PreferenceTextFieldGroup {
                        DimensionInput(LockScreenShortcutGlassSettings.WIDTH_KEY, R.string.systemui_shortcut_glass_width)
                        DimensionInput(LockScreenShortcutGlassSettings.HEIGHT_KEY, R.string.systemui_shortcut_glass_height)
                        DimensionInput(LockScreenShortcutGlassSettings.RADIUS_KEY, R.string.systemui_shortcut_glass_radius)
                    }
                }
            }
        }
    }

    @Composable
    private fun DimensionInput(key: String, label: Int) {
        val repository = LocalPreferencesRepository.current
        var value by remember(key) { mutableStateOf(repository.getString(key)) }
        val isRadius = key == LockScreenShortcutGlassSettings.RADIUS_KEY
        PreferenceTextField(
            label = stringResource(label),
            value = value,
            onValueChange = {
                value = it
                repository.putString(key, it)
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(if (isRadius) "28" else "56") },
            supportingText = {
                Text(stringResource(if (isRadius) R.string.systemui_shortcut_glass_radius_hint else R.string.systemui_shortcut_glass_size_hint))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
    }

    override fun HookContext.onHook() {
        val preferences = prefs(PreferenceKeys.FILE_NAME)
        val settings = LockScreenShortcutGlassSettings.from(
            preferences.getString(LockScreenShortcutGlassSettings.WIDTH_KEY),
            preferences.getString(LockScreenShortcutGlassSettings.HEIGHT_KEY),
            preferences.getString(LockScreenShortcutGlassSettings.RADIUS_KEY),
        )
        // The shortcut plugin is supplied by com.miui.aod but runs inside SystemUI.
        val hookedLoaders = Collections.newSetFromMap(WeakHashMap<ClassLoader, Boolean>())
        val material = ShortcutGlassMaterial(classLoader)
        Log.i(TAG, "Registering lock-screen shortcut glass hooks")

        hookSafely("MiuiShortcutController.onPluginLoaded") {
            val loaded = "com.android.keyguard.shortcut.MiuiShortcutController".toClass().resolve()
                .firstMethod {
                    name = "onPluginLoaded"
                    parameterCount = 3
                }
                .self
            xposed.hook(loaded).intercept { chain ->
                val plugin = chain.args[0]
                val loader = plugin?.javaClass?.classLoader
                if (loader != null && hookedLoaders.add(loader)) {
                    hookSafely("ShortcutViewLayoutController") {
                        val controllerClass = loader.loadClass(
                            "com.miui.keyguard.shortcuts.controller.ShortcutViewLayoutController",
                        )
                        listOf("updateShortcutView", "updateShortcutFixedSize", "updateShortcutViewImageTintList").forEach { name ->
                            val method = controllerClass.declaredMethods.first { it.name == name }
                            xposed.hook(method).intercept { update ->
                                val result = update.proceed()
                                update.thisObject?.let { controller ->
                                    applyBackgrounds(controller, settings, material)
                                }
                                result
                            }
                        }
                    }
                }
                val result = chain.proceed()
                // Plugin onCreate and its first layout can precede onPluginLoaded.
                runCatching { plugin?.field("shortcutViewLayoutController") }.getOrNull()?.let {
                    applyBackgrounds(it, settings, material)
                }
                result
            }
        }
    }

    private fun applyBackgrounds(
        controller: Any,
        settings: LockScreenShortcutGlassSettings,
        material: ShortcutGlassMaterial,
    ) {
        runCatching {
            val context = controller.field("sysuiContext") as Context
            listOf("shortcutViewLeft", "shortcutViewRight").forEach { name ->
                (controller.field(name) as? ImageView)?.let { icon ->
                    ShortcutGlassBackground.install(
                        icon = icon,
                        context = context,
                        settings = settings,
                        material = material,
                    )
                }
            }
        }.onFailure { Log.e(TAG, "Unable to apply lock-screen shortcut glass", it) }
    }

    private companion object {
        const val TAG = "LockScreenShortcutGlass"
    }
}
