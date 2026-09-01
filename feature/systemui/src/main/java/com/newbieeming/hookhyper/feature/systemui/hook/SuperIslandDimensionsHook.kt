package com.newbieeming.hookhyper.feature.systemui.hook

import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
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
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.core.ui.component.PreferenceTextField
import com.newbieeming.hookhyper.core.ui.component.PreferenceTextFieldGroup
import com.newbieeming.hookhyper.feature.systemui.R
import com.newbieeming.hookhyper.feature.systemui.SystemUiFeatureEntry
import com.newbieeming.hookhyper.feature.systemui.model.SystemUiHookDef
import kotlin.math.roundToInt

/** 覆盖 HyperOS 界面组件中已验证的超级岛 dimen. */
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
                modifier = Modifier.padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DimensionGroup.entries.forEach { group ->
                    Text(
                        text = stringResource(group.titleResId),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    PreferenceTextFieldGroup {
                        DIMENSIONS.filter { it.group == group }.forEach { dimension ->
                            DimensionInput(dimension)
                        }
                    }
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
        PreferenceTextField(
            value = value,
            onValueChange = {
                value = it
                repository.putString(dimension.preferenceKey, it)
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            label = stringResource(dimension.descriptionResId),
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
        val group: DimensionGroup,
    ) {
        val preferenceKey = "systemui_super_island_$resourceName"
    }

    private enum class DimensionGroup(@StringRes val titleResId: Int) {
        COLLAPSED(R.string.super_island_collapsed_dimensions),
        EXPANDED(R.string.super_island_expanded_dimensions),
        DRAG_CARD(R.string.super_island_drag_card_dimensions),
        SHARE(R.string.super_island_share_dimensions),
    }

    private companion object {
        private const val PLUGIN_PACKAGE = "miui.systemui.plugin"

        @Suppress("MagicNumber")
        private val DIMENSIONS = listOf(
            IslandDimension(
                resourceName = "big_island_min_width",
                descriptionResId = R.string.super_island_big_island_min_width,
                defaultDp = 108f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_height",
                descriptionResId = R.string.super_island_height,
                defaultDp = 34f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_app_icon_size",
                descriptionResId = R.string.super_island_app_icon_size,
                defaultDp = 19f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_area_padding",
                descriptionResId = R.string.super_island_area_padding,
                defaultDp = 8f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_area_padding_cutout",
                descriptionResId = R.string.super_island_area_padding_cutout,
                defaultDp = 1f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_fix_icon_margin",
                descriptionResId = R.string.super_island_fix_icon_margin,
                defaultDp = 1f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_fix_icon_size",
                descriptionResId = R.string.super_island_fix_icon_size,
                defaultDp = 20f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_icon_radius",
                descriptionResId = R.string.super_island_icon_radius,
                defaultDp = 4.5599976f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_mini_y",
                descriptionResId = R.string.super_island_mini_y,
                defaultDp = 3f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_radius",
                descriptionResId = R.string.super_island_radius,
                defaultDp = 30f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_swipe_threshold",
                descriptionResId = R.string.super_island_swipe_threshold,
                defaultDp = 50f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_title_size",
                descriptionResId = R.string.super_island_title_size,
                defaultDp = 14f,
                group = DimensionGroup.COLLAPSED,
            ),
            IslandDimension(
                resourceName = "island_expanded_padding_top",
                descriptionResId = R.string.super_island_expanded_padding_top,
                defaultDp = 4f,
                group = DimensionGroup.EXPANDED,
            ),
            IslandDimension(
                resourceName = "isLand_drag_card_bg_margin",
                descriptionResId = R.string.super_island_drag_card_bg_margin,
                defaultDp = 40f,
                group = DimensionGroup.DRAG_CARD,
            ),
            IslandDimension(
                resourceName = "isLand_drag_card_bg_radius",
                descriptionResId = R.string.super_island_drag_card_bg_radius,
                defaultDp = 22f,
                group = DimensionGroup.DRAG_CARD,
            ),
            IslandDimension(
                resourceName = "isLand_drag_card_hand_margin_right",
                descriptionResId = R.string.super_island_drag_card_hand_margin_right,
                defaultDp = 68f,
                group = DimensionGroup.DRAG_CARD,
            ),
            IslandDimension(
                resourceName = "isLand_drag_card_hand_margin_top",
                descriptionResId = R.string.super_island_drag_card_hand_margin_top,
                defaultDp = 10f,
                group = DimensionGroup.DRAG_CARD,
            ),
            IslandDimension(
                resourceName = "isLand_drag_card_imageview_height_and_width",
                descriptionResId = R.string.super_island_drag_card_image_size,
                defaultDp = 46f,
                group = DimensionGroup.DRAG_CARD,
            ),
            IslandDimension(
                resourceName = "isLand_drag_card_linear_layout_margin_start",
                descriptionResId = R.string.super_island_drag_card_content_margin_start,
                defaultDp = 10f,
                group = DimensionGroup.DRAG_CARD,
            ),
            IslandDimension(
                resourceName = "isLand_drag_card_padding",
                descriptionResId = R.string.super_island_drag_card_padding,
                defaultDp = 40f,
                group = DimensionGroup.DRAG_CARD,
            ),
            IslandDimension(
                "isLand_drag_card_shadow_margin_left_and_right",
                R.string.super_island_drag_card_shadow_horizontal_margin,
                -40f,
                DimensionGroup.DRAG_CARD,
            ),
            IslandDimension(
                resourceName = "isLand_drag_card_text_view_max_width",
                descriptionResId = R.string.super_island_drag_card_text_max_width,
                defaultDp = 144f,
                group = DimensionGroup.DRAG_CARD,
            ),
            IslandDimension(
                resourceName = "isLand_drag_card_title_margin_start",
                descriptionResId = R.string.super_island_drag_card_title_margin_start,
                defaultDp = 15f,
                group = DimensionGroup.DRAG_CARD,
            ),
            IslandDimension(
                resourceName = "island_share_pic_radius",
                descriptionResId = R.string.super_island_share_pic_radius,
                defaultDp = 10f,
                group = DimensionGroup.SHARE,
            ),
            IslandDimension(
                resourceName = "island_share_view_height",
                descriptionResId = R.string.super_island_share_view_height,
                defaultDp = 190f,
                group = DimensionGroup.SHARE,
            ),
            IslandDimension(
                resourceName = "island_share_view_width",
                descriptionResId = R.string.super_island_share_view_width,
                defaultDp = 310f,
                group = DimensionGroup.SHARE,
            ),
        )
    }
}
