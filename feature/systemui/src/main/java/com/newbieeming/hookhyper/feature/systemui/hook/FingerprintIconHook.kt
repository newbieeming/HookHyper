package com.newbieeming.hookhyper.feature.systemui.hook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.core.ui.component.SettingsPreferenceGroup
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.model.FingerprintIconStyle
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef

@Suppress("LabeledExpression")
// API 102 does not provide Yuki resource injection. Keep unregistered until reimplemented.
// @HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class FingerprintIconHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {

    override val def = SystemUiHookDef.REPLACE_FINGERPRINT_ICON

    @Composable
    override fun Content() {
        val repository = LocalPreferencesRepository.current
        var enabled by remember { mutableStateOf(repository.getBoolean(preferenceKey)) }
        var style by remember {
            mutableStateOf(FingerprintIconStyle.fromId(repository.getString(FingerprintIconStyle.PREFERENCE_KEY)))
        }
        SettingsPreferenceGroup(index = 0, count = 2) {
            HookSwitchPreference(
                preferenceKey = preferenceKey,
                title = stringResource(R.string.systemui_replace_fingerprint_icon_title),
                summary = stringResource(R.string.systemui_replace_fingerprint_icon_summary),
                onCheckedChange = { enabled = it },
            )
            AnimatedVisibility(
                visible = enabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                FingerprintStyleSelector(
                    selected = style,
                    onSelect = {
                        style = it
                        repository.putString(FingerprintIconStyle.PREFERENCE_KEY, it.id)
                    },
                )
            }
        }
    }

    override fun HookContext.onHook() {
        // TODO API 102: no host resource injection API; keep this implementation disabled.
        // val preferences = prefs(PreferenceKeys.FILE_NAME)
        // // 注入模块资源，使模块 R.drawable.xxx 在宿主中可用
        // onAppLifecycle {
        //     onCreate {
        //         injectModuleAppResources()
        //     }
        // }
        // // hook getFingerIconResource 直接替换返回的资源 ID
        // "com.miui.keyguard.biometrics.fod.MiuiGxzwAnimManager".toClass().resolve().firstMethod {
        //     name = "getFingerIconResource"
        //     parameterCount = 1
        // }.hook {
        //     after {
        //         val original = result as? Int ?: return@after
        //         val res = appResources ?: return@after
        //         val resName = runCatching {
        //             res.getResourceEntryName(original)
        //         }.getOrNull() ?: return@after
        //         val style =
        //             FingerprintIconStyle.fromId(preferences.getString(FingerprintIconStyle.PREFERENCE_KEY))
        //         style.replacementFor(resName)?.let { moduleResId ->
        //             result = moduleResId
        //             Log.d(
        //                 TAG,
        //                 "Replaced $resName with ${style.id}: 0x${Integer.toHexString(moduleResId)}"
        //             )
        //         }
        //     }
        // }
    }

    // private companion object {
    //     private const val TAG = "FingerprintIconHook"
    // }
}

@Composable
private fun FingerprintStyleSelector(
    selected: FingerprintIconStyle,
    onSelect: (FingerprintIconStyle) -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.systemui_fingerprint_style_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FingerprintStyleOption(
                    style = FingerprintIconStyle.CIRCLE,
                    selected = selected == FingerprintIconStyle.CIRCLE,
                    darkTheme = darkTheme,
                    onClick = { onSelect(FingerprintIconStyle.CIRCLE) },
                )
                FingerprintStyleOption(
                    style = FingerprintIconStyle.VANILLA,
                    selected = selected == FingerprintIconStyle.VANILLA,
                    darkTheme = darkTheme,
                    onClick = { onSelect(FingerprintIconStyle.VANILLA) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.FingerprintStyleOption(
    style: FingerprintIconStyle,
    selected: Boolean,
    darkTheme: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colorScheme.primaryContainer else colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(if (darkTheme) style.normalResId else style.lightResId),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = stringResource(style.labelResId()),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

private fun FingerprintIconStyle.labelResId(): Int = when (this) {
    FingerprintIconStyle.CIRCLE -> R.string.systemui_fingerprint_style_circle
    FingerprintIconStyle.VANILLA -> R.string.systemui_fingerprint_style_vanilla
}
