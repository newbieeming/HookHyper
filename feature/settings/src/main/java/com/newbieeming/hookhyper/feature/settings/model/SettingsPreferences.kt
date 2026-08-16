package com.newbieeming.hookhyper.feature.settings.model

object SettingsPreferenceKeys {
    const val EDIT_DEVICE_INFO = "settings_edit_device_info"
}

data class DeviceInfoField(
    val desc: String,
    val preferenceKey: String,
    val stringsName: String = "",
)

object DeviceInfoFields {
    val deviceName = DeviceInfoField(
        desc = "设备型号",
        preferenceKey = "settings_device_name",
        stringsName = "model_number",
    )
    val cpuName = DeviceInfoField(
        desc = "处理器",
        preferenceKey = "settings_device_cpu_name",
        stringsName = "device_cpu",
    )
    val cpuDetail = DeviceInfoField(
        desc = "处理器",
        preferenceKey = "settings_device_cpu_detail",
        stringsName = "device_cpu"
    )
    val memory = DeviceInfoField(
        desc = "运行内存",
        preferenceKey = "settings_device_memory",
        stringsName = "device_memory",
    )
    val batteryCapacity = DeviceInfoField(
        desc = "电池容量",
        preferenceKey = "settings_device_battery",
        stringsName = "appfunc_name_battery_capacity",
    )
    val resolution = DeviceInfoField(
        desc = "分辨率",
        preferenceKey = "settings_device_screen_resolution",
        stringsName = "device_screen_resolution",
    )
    val screenSize = DeviceInfoField(
        desc = "屏幕尺寸",
        preferenceKey = "settings_device_screen_size",
        stringsName = "device_screen_size",
    )
    val camera = DeviceInfoField(
        desc = "摄像头",
        preferenceKey = "settings_device_camera",
        stringsName = "device_camera",
    )
    val osVersion = DeviceInfoField(
        desc = "OS版本",
        preferenceKey = "settings_device_os_version",
        stringsName = "device_miui_version",
    )
    val certModel = DeviceInfoField(
        desc = "认证型号",
        preferenceKey = "settings_device_cert_model",
        stringsName = "model_name",
    )
    val hardwareVersion = DeviceInfoField(
        desc = "硬件版本",
        preferenceKey = "settings_device_hardware_version",
        stringsName = "hardware_version",
    )
    val baseband = DeviceInfoField(
        desc = "基带版本",
        preferenceKey = "settings_device_baseband",
    )
    val androidVersion = DeviceInfoField(
        desc = "Android 版本",
        preferenceKey = "settings_device_android_version",
    )
    val kernelVersion = DeviceInfoField(
        desc = "内核版本",
        preferenceKey = "settings_device_kernel_version",
    )

    val all = listOf(
        deviceName,
        cpuName,
        cpuDetail,
        memory,
        batteryCapacity,
        resolution,
        screenSize,
        camera,
        osVersion,
        certModel,
        hardwareVersion,
        baseband,
        androidVersion,
        kernelVersion,
    )

    /**
     * 仅当 stringsName 为空时，通过 value 正则匹配字段。
     */
    fun preferenceKeyForValue(value: String): String? = when {
        // 处理器详情：含 GHz
        value.contains("GHz", ignoreCase = true) -> cpuDetail.preferenceKey
        // 处理器简称：含平台关键词但不含 GHz
        CPU_REGEX.containsMatchIn(value) -> cpuName.preferenceKey
        // 基带版本：含 MPSS
        BASEBAND_REGEX.containsMatchIn(value) -> baseband.preferenceKey
        // Android 版本：格式如 "17 | Android 安全更新：..."
        ANDROID_VERSION_REGEX.containsMatchIn(value) -> androidVersion.preferenceKey
        // 内核版本：格式如 "6.12.69-android16-..."
        KERNEL_VERSION_REGEX.containsMatchIn(value) -> kernelVersion.preferenceKey
        else -> null
    }

    /**
     * 通过 stringsName 从应用资源解析实际字符串，匹配 all 中的字段。
     * @param resolve 资源名 → 实际字符串的解析函数
     */
    fun matchByTitle(title: String, resolve: (String) -> String?): List<DeviceInfoField> {
        val normalizedTitle = title.normalizedTitle()
        return all.filter { field ->
            if (field.stringsName.isBlank()) return@filter false
            val resolved = resolve(field.stringsName) ?: return@filter false
            resolved.normalizedTitle() == normalizedTitle
        }
    }

    /**
     * 统一匹配：stringsName 在 all 中唯一命中则用 stringsName 结果，否则走 value 正则。
     * @param resolve 资源名 → 实际字符串的解析函数
     */
    fun resolveKey(title: String, value: String, resolve: (String) -> String?): String? {
        if (title.isNotBlank()) {
            val matches = matchByTitle(title, resolve)
            if (matches.size == 1) return matches.first().preferenceKey
        }
        return preferenceKeyForValue(value)
    }

    private fun String.normalizedTitle() =
        filterNot(Char::isWhitespace).lowercase()

    private val CPU_REGEX = Regex(
        """平台|澎湃|高通|天玑|骁龙|玄戒|Snapdragon|Dimensity|MediaTek|Qualcomm|Mobile\s+Platform""",
        RegexOption.IGNORE_CASE,
    )
    // 基带版本：高通 MPSS、联发科 MOLY、展锐 SRNC/SP、通用 MODEM 等
    private val BASEBAND_REGEX = Regex(
        """\b(?:MPSS|MOLY|MODEM|SRNC|BP_)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val ANDROID_VERSION_REGEX = Regex("""^\d+\s*\|""")
    private val KERNEL_VERSION_REGEX = Regex("""^\d+\.\d+\.\d+-android\d+""")
}
