package com.newbieeming.hookhyper.feature.settings.hook

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.hook.HookModule
import com.newbieeming.hookhyper.core.hook.HookUtils.call
import com.newbieeming.hookhyper.core.hook.SubHooker
import com.newbieeming.hookhyper.core.ui.component.FeatureHook
import com.newbieeming.hookhyper.core.ui.component.HookSwitchPreference
import com.newbieeming.hookhyper.core.ui.component.LocalPreferencesRepository
import com.newbieeming.hookhyper.core.ui.component.SettingsPreferenceGroup
import com.newbieeming.hookhyper.core.ui.feature.featureViewModel
import com.newbieeming.hookhyper.feature.settings.R
import com.newbieeming.hookhyper.feature.settings.SettingsFeatureEntry
import com.newbieeming.hookhyper.feature.settings.SettingsFeatureIntent
import com.newbieeming.hookhyper.feature.settings.SettingsFeatureViewModel
import com.newbieeming.hookhyper.feature.settings.model.DeviceInfoFields
import com.newbieeming.hookhyper.feature.settings.model.SettingsHookDef
import com.newbieeming.hookhyper.feature.settings.ui.DeviceInfoSectionTitle
import com.newbieeming.hookhyper.feature.settings.ui.DeviceInfoTextField
import com.newbieeming.hookhyper.feature.settings.ui.DeviceInfoTextFieldGroup

@HookModule(packageName = SettingsFeatureEntry.PACKAGE_NAME)
class DeviceInfoHook :
    SubHooker,
    FeatureHook<SettingsHookDef> {

    override val def = SettingsHookDef.EDIT_DEVICE_INFO

    private companion object {
        private const val CARD_INFO = "com.android.settings.device.DeviceCardInfo"
        private const val BASE_CARD = "com.android.settings.device.BaseDeviceCardItem"
        private const val ABOUT_PHONE = "com.android.settings.device.MiuiAboutPhoneUtils"
    }

    @Composable
    override fun Content() {
        val repo = LocalPreferencesRepository.current
        var enabled by remember { mutableStateOf(repo.getBoolean(preferenceKey)) }

        SettingsPreferenceGroup {
            HookSwitchPreference(
                preferenceKey = preferenceKey,
                title = stringResource(R.string.settings_edit_device_info_title),
                summary = stringResource(R.string.settings_edit_device_info_summary),
                onCheckedChange = { enabled = it },
            )
            AnimatedVisibility(
                visible = enabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                DeviceInfoFieldList()
            }
        }
    }

    @Composable
    private fun DeviceInfoFieldList() {
        val viewModel = featureViewModel<SettingsFeatureViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val fields = DeviceInfoFields.all
        val focusRequesters = remember(fields.size) { List(fields.size) { FocusRequester() } }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val sectionTitles = mapOf(
            DeviceInfoFields.deviceName to R.string.device_info_section_hardware,
            DeviceInfoFields.osVersion to R.string.device_info_section_system,
            DeviceInfoFields.certModel to R.string.device_info_section_versions,
        )

        DeviceInfoTextFieldGroup {
            fields.forEachIndexed { index, field ->
                sectionTitles[field]?.let { DeviceInfoSectionTitle(stringResource(it)) }
                DeviceInfoTextField(
                    label = deviceInfoLabel(field.preferenceKey),
                    value = state.values[field.preferenceKey].orEmpty(),
                    onValueChange = { viewModel.accept(SettingsFeatureIntent.UpdateValue(field.preferenceKey, it)) },
                    modifier = Modifier.focusRequester(focusRequesters[index]),
                    supportingText = field.originValue.takeIf { it.isNotBlank() }
                        ?.let { stringResource(R.string.device_info_origin_value, it) },
                    imeAction = if (index == fields.lastIndex) ImeAction.Done else ImeAction.Next,
                    onImeAction = {
                        if (index == fields.lastIndex) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        } else {
                            focusRequesters[index + 1].requestFocus()
                        }
                    },
                )
            }
        }
    }

    @Composable
    private fun deviceInfoLabel(preferenceKey: String): String {
        val resourceId = when (preferenceKey) {
            "settings_device_name" -> R.string.device_name
            "settings_device_cpu_name" -> R.string.device_processor
            "settings_device_cpu_detail" -> R.string.device_processor_detail
            "settings_device_memory" -> R.string.device_memory
            "settings_device_battery" -> R.string.device_battery_capacity
            "settings_device_screen_resolution" -> R.string.device_resolution
            "settings_device_screen_size" -> R.string.device_screen_size
            "settings_device_os_version" -> R.string.device_os_version
            "settings_device_os_xms_version" -> R.string.device_xms_version
            "settings_device_os_ro_xms_version" -> R.string.device_ro_xms_version
            "settings_device_camera" -> R.string.device_camera
            "settings_device_baseband" -> R.string.device_baseband
            "settings_device_cert_model" -> R.string.device_cert_model
            "settings_device_hardware_version" -> R.string.device_hardware_version
            "settings_device_android_version" -> R.string.device_android_version
            "settings_device_kernel_version" -> R.string.device_kernel_version
            else -> error("Unsupported device info preference: $preferenceKey")
        }
        return stringResource(resourceId)
    }

    override fun HookContext.onHook() {
        var resources: Resources? = null
        val attach = Application::class.java.getDeclaredMethod("attach", Context::class.java)
        xposed.hook(attach).intercept { chain ->
            val context = chain.args[0] as Context
            if (context.packageName == packageName) resources = context.resources
            chain.proceed()
        }
        hookDeviceCard { resources }
        hookVersionInfo()
    }

    private fun HookContext.hookDeviceCard(resources: () -> Resources?) {
        val featurePreferences = prefs(PreferenceKeys.FILE_NAME)
        val resolveString: (String) -> String? = { name ->
            runCatching {
                val appRes = resources()
                val resId = appRes?.getIdentifier(
                    name,
                    "string",
                    SettingsFeatureEntry.PACKAGE_NAME,
                ) ?: 0
                if (resId != 0) appRes?.getString(resId) else null
            }.getOrNull()
        }

        hookSafely("BaseDeviceCardItem.setValue") {
            // 特殊处理摄像头FirstValue、SecondValue
            BASE_CARD.toClass().resolve().optional(silent = true).method {
                name = "setValue"
                parameterCount = 2
            }.forEach { method ->
                xposed.hook(method.self).intercept { chain ->
                    val info = chain.args[0]
                    val title = info?.call("getTitle")?.toString().orEmpty()
                    val str = resolveString(DeviceInfoFields.camera.stringsName)
                    if (title != str) return@intercept chain.proceed()
                    val value = info?.call("getValue")?.toString().orEmpty()
                    info?.call("setValue", value)
                    if (info?.call("getFirstValue") != null) {
                        info.call("setFirstValue", value)
                        info.call("setSecondValue", "")
                    }
                    chain.proceed()
                }
            }
        }

        hookSafely("DeviceCardInfo.setValue") {
            val method = CARD_INFO.toClass().resolve().firstMethod {
                name = "setValue"
                parameters(String::class)
            }.self
            xposed.hook(method).intercept { chain ->
                    val cardInfo = requireNotNull(chain.thisObject)
                    val title = cardInfo.call("getTitle")?.toString()?.trim().orEmpty()
                    val value = chain.args[0]?.toString().orEmpty()
                    val key = DeviceInfoFields.resolveKey(title, value, resolveString)
                        ?: return@intercept chain.proceed()
                    val replacement = featurePreferences.getString(key).takeIf(String::isNotBlank)
                        ?: return@intercept chain.proceed()
                    val arguments = chain.args.toTypedArray()
                    arguments[0] = replacement
                    chain.proceed(arguments)
            }
        }
    }

    private fun HookContext.hookVersionInfo() {
        val featurePreferences = prefs(PreferenceKeys.FILE_NAME)
        val aboutPhone = ABOUT_PHONE.toClass().resolve()

        listOf(
            "getOsVersionCode" to DeviceInfoFields.osVersion.preferenceKey,
            "getRoXmsVersion" to DeviceInfoFields.roXmsVersion.preferenceKey,
            "getXmsVersion" to DeviceInfoFields.xmsVersion.preferenceKey,
        ).forEach { (methodName, prefKey) ->
            hookSafely("MiuiAboutPhoneUtils.$methodName") {
                val method = aboutPhone.firstMethod {
                    name = methodName
                    emptyParameters()
                }.self
                xposed.hook(method).intercept { chain ->
                    val original = chain.proceed()
                    featurePreferences.getString(prefKey).takeIf(String::isNotBlank) ?: original
                }
            }
        }
    }
}
