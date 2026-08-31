package com.newbieeming.hookhyper.feature.systemui.hook

import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
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
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef
import kotlin.math.roundToInt

/** 覆盖 HyperOS 界面组件中已验证的超级岛 dimen。 */
@HookModule(packageName = SystemUiFeatureEntry.PACKAGE_NAME)
class SuperIslandDimensionsHook :
    SubHooker,
    FeatureHook<SystemUiHookDef> {

    override val def = SystemUiHookDef.SUPER_ISLAND_DIMENSIONS

    @Composable
    override fun Content() {
        val repository = LocalPreferencesRepository.current
        var expanded by remember { mutableStateOf(repository.getBoolean(preferenceKey)) }
        HookSwitchPreference(
            preferenceKey = preferenceKey,
            title = stringResource(R.string.systemui_super_island_dimensions_title),
            summary = stringResource(R.string.systemui_super_island_dimensions_summary),
            onCheckedChange = { expanded = it },
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                DIMENSIONS.forEach { dimension ->
                    DimensionInput(dimension)
                }
            }
        }
    }

    @Composable
    private fun DimensionInput(dimension: IslandDimension) {
        val repository = LocalPreferencesRepository.current
        var value by remember(dimension.preferenceKey) {
            mutableStateOf(repository.getString(dimension.preferenceKey))
        }
        OutlinedTextField(
            value = value,
            onValueChange = {
                value = it
                repository.putString(dimension.preferenceKey, it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(dimension.descriptionResId)) },
            supportingText = {
                Text(
                    stringResource(
                        R.string.super_island_dimension_hint,
                        dimension.defaultDp,
                    ),
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
    }

    override fun PackageParam.onHook() {
        val preferences = prefs(PreferenceKeys.FILE_NAME)
        val overrides = DIMENSIONS.mapNotNull { dimension ->
            preferences.getString(dimension.preferenceKey).toFloatOrNull()?.let {
                dimension.resourceName to it
            }
        }.toMap()
        if (overrides.isEmpty()) return

        Resources::class.java.resolve().firstMethod {
            name = "getDimension"
            parameterCount = 1
        }.hook {
            after {
                overrideDimension(instance<Resources>(), args(0).int(), overrides)?.let { value ->
                    result = value
                }
            }
        }
        Resources::class.java.resolve().firstMethod {
            name = "getDimensionPixelOffset"
            parameterCount = 1
        }.hook {
            after {
                overrideDimension(instance<Resources>(), args(0).int(), overrides)?.let { value ->
                    result = value.toInt()
                }
            }
        }
        Resources::class.java.resolve().firstMethod {
            name = "getDimensionPixelSize"
            parameterCount = 1
        }.hook {
            after {
                overrideDimension(instance<Resources>(), args(0).int(), overrides)?.let { value ->
                    result = value.roundToInt()
                }
            }
        }
    }

    private fun overrideDimension(
        resources: Resources,
        resourceId: Int,
        overrides: Map<String, Float>,
    ): Float? = runCatching {
        if (resources.getResourcePackageName(resourceId) != PLUGIN_PACKAGE) return null
        val dp = overrides[resources.getResourceEntryName(resourceId)] ?: return null
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }.getOrNull()

    private data class IslandDimension(
        val resourceName: String,
        @StringRes val descriptionResId: Int,
        val defaultDp: Float,
    ) {
        val preferenceKey = "systemui_super_island_$resourceName"
    }

    private companion object {
        private const val PLUGIN_PACKAGE = "miui.systemui.plugin"

        private val DIMENSIONS = listOf(
            IslandDimension("big_island_min_width", R.string.super_island_big_island_min_width, 108f),
            IslandDimension("island_height", R.string.super_island_height, 34f),
        )
    }
}
