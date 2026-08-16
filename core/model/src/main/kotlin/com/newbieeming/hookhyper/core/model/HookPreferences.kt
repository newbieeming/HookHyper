package com.newbieeming.hookhyper.core.model

enum class UiStyle {
    MIUIX,
    MATERIAL,
}

object PreferenceKeys {
    const val FILE_NAME = "hookhyper_prefs"
    const val UI_STYLE = "ui_style"

    const val SYSTEMUI_LOCK_SHOW_SIM_NAME = "systemui_lock_show_sim_name"
    const val SYSTEMUI_FORCE_SOFT_LIGHT_GLASS = "systemui_force_soft_light_glass"

    const val SETTINGS_EDIT_DEVICE_INFO = "settings_edit_device_info"
}

data class DeviceInfoField(
    val title: String,
    val preferenceKey: String,
    val titleAliases: Set<String> = emptySet(),
)

object DeviceInfoFields {
    val deviceName = DeviceInfoField(
        title = "设备名称",
        preferenceKey = "settings_device_name",
        titleAliases = setOf("Device name"),
    )
    val processor = DeviceInfoField(
        title = "处理器",
        preferenceKey = "settings_device_cpu",
        titleAliases = setOf("CPU", "Processor"),
    )
    val memory = DeviceInfoField(
        title = "运行内存",
        preferenceKey = "settings_device_memory",
        titleAliases = setOf("RAM", "Memory"),
    )
    val batteryCapacity = DeviceInfoField(
        title = "电池容量",
        preferenceKey = "settings_device_battery",
        titleAliases = setOf("Battery capacity"),
    )
    val resolution = DeviceInfoField(
        title = "分辨率",
        preferenceKey = "settings_device_screen_resolution",
        titleAliases = setOf("Resolution"),
    )
    val screenSize = DeviceInfoField(
        title = "屏幕尺寸",
        preferenceKey = "settings_device_screen_size",
        titleAliases = setOf("Screen size"),
    )
    val osVersion = DeviceInfoField(
        title = "OS版本",
        preferenceKey = "settings_device_os_version",
        titleAliases = setOf("OS 版本", "OS version"),
    )
    val camera = DeviceInfoField(
        title = "摄像头",
        preferenceKey = "settings_device_camera",
        titleAliases = setOf("Camera", "Camera specs"),
    )
    val baseband = DeviceInfoField(
        title = "基带",
        preferenceKey = "settings_device_baseband",
        titleAliases = setOf("Baseband", "Baseband version"),
    )

    val all = listOf(
        deviceName,
        processor,
        memory,
        batteryCapacity,
        resolution,
        screenSize,
        osVersion,
        camera,
        baseband,
    )

    fun preferenceKeyForValue(value: String): String? = when {
        MEMORY_REGEX.containsMatchIn(value) -> memory.preferenceKey
        BATTERY_REGEX.containsMatchIn(value) -> batteryCapacity.preferenceKey
        RESOLUTION_REGEX.containsMatchIn(value) -> resolution.preferenceKey
        SCREEN_SIZE_REGEX.containsMatchIn(value) -> screenSize.preferenceKey
        CPU_REGEX.containsMatchIn(value) -> processor.preferenceKey
        BASEBAND_REGEX.containsMatchIn(value) -> baseband.preferenceKey
        CAMERA_REGEX.containsMatchIn(value) -> camera.preferenceKey
        DEVICE_NAME_REGEX.containsMatchIn(value) -> deviceName.preferenceKey
        OS_VERSION_REGEX.containsMatchIn(value) -> osVersion.preferenceKey
        else -> null
    }

    fun preferenceKeyForTitle(title: String): String? {
        val normalizedTitle = title.normalizedTitle()
        return all.firstOrNull { field ->
            (field.titleAliases + field.title).any { it.normalizedTitle() == normalizedTitle }
        }?.preferenceKey
    }

    private fun String.normalizedTitle() =
        filterNot(Char::isWhitespace).lowercase()

    private val MEMORY_REGEX = Regex("""\d+(?:\.\d+)?\s*\+\s*\d+(?:\.\d+)?\s*GB\b""", RegexOption.IGNORE_CASE)
    private val BATTERY_REGEX = Regex("""\d+\s*mAh\b""", RegexOption.IGNORE_CASE)
    private val SCREEN_SIZE_REGEX = Regex("""(?:英寸|["”″]|\binches?\b)""", RegexOption.IGNORE_CASE)
    private val RESOLUTION_REGEX = Regex("""\d+\s*[*x×]\s*\d+""", RegexOption.IGNORE_CASE)
    private val CPU_REGEX = Regex(
        """(?:平台|澎湃|高通|天玑|骁龙|玄戒|Snapdragon|Dimensity|MediaTek|Qualcomm|Mobile\s+Platform)""",
        RegexOption.IGNORE_CASE,
    )
    private val BASEBAND_REGEX = Regex("""\bMPSS\b""", RegexOption.IGNORE_CASE)
    private val CAMERA_REGEX = Regex("""\d+\s*MP\b""", RegexOption.IGNORE_CASE)
    private val DEVICE_NAME_REGEX = Regex("""\b(?:Xiaomi|Redmi|POCO)\b""", RegexOption.IGNORE_CASE)
    private val OS_VERSION_REGEX = Regex("""^\d+\.\d+\.\d+\.\d+\.\S+""")
}
