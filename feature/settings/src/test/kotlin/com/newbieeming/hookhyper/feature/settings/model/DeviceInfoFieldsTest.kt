package com.newbieeming.hookhyper.feature.settings.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeviceInfoFieldsTest {
    // 模拟应用资源解析：stringsName → 中文字符串
    private val mockResolve: (String) -> String? = { name ->
        when (name) {
            "model_number" -> "设备型号"
            "device_cpu" -> "处理器"
            "device_memory" -> "运行内存"
            "appfunc_name_battery_capacity" -> "电池容量"
            "device_screen_resolution" -> "分辨率"
            "device_screen_size" -> "屏幕尺寸"
            "device_miui_version" -> "OS版本"
            "device_camera" -> "摄像头"
            "model_name" -> "认证型号"
            "hardware_version" -> "硬件版本"
            else -> null
        }
    }

    @Test
    fun `unique title match uses resolved resource string`() {
        assertEquals("settings_device_name", DeviceInfoFields.resolveKey("设备型号", "", mockResolve))
        assertEquals("settings_device_memory", DeviceInfoFields.resolveKey("运行内存", "", mockResolve))
        assertEquals("settings_device_battery", DeviceInfoFields.resolveKey("电池容量", "", mockResolve))
        assertEquals("settings_device_screen_size", DeviceInfoFields.resolveKey("屏幕尺寸", "", mockResolve))
        assertEquals("settings_device_screen_resolution", DeviceInfoFields.resolveKey("分辨率", "", mockResolve))
        assertEquals("settings_device_os_version", DeviceInfoFields.resolveKey("OS版本", "", mockResolve))
        assertEquals("settings_device_camera", DeviceInfoFields.resolveKey("摄像头", "", mockResolve))
        assertEquals("settings_device_cert_model", DeviceInfoFields.resolveKey("认证型号", "", mockResolve))
        assertEquals("settings_device_hardware_version", DeviceInfoFields.resolveKey("硬件版本", "", mockResolve))
    }

    @Test
    fun `duplicate title falls back to value - cpu`() {
        // 处理器 title 命中 cpuName + cpuDetail 两个 → 走 value
        // 不含 GHz → cpuName（简称）
        assertEquals(
            "settings_device_cpu_name",
            DeviceInfoFields.resolveKey("处理器", "第五代骁龙®8至尊版移动平台", mockResolve),
        )
        // 含 GHz → cpuDetail（详情）
        assertEquals(
            "settings_device_cpu_detail",
            DeviceInfoFields.resolveKey("处理器", "第五代骁龙®8至尊版移动平台\n八核 最高 4.6GHz", mockResolve),
        )
    }

    @Test
    fun `no title falls back to value regex`() {
        assertEquals("settings_device_baseband", DeviceInfoFields.resolveKey("", "MPSS.DE.9.0-e64d0ee8fe", mockResolve))
        assertEquals("settings_device_android_version", DeviceInfoFields.resolveKey("", "17 | Android 安全更新：2026年8月1日", mockResolve))
        assertEquals(
            "settings_device_kernel_version",
            DeviceInfoFields.resolveKey("", "6.12.69-android16-6-gb1493ec68d4a-abogki514973465-4k", mockResolve),
        )
    }

    @Test
    fun `no match returns null`() {
        assertNull(DeviceInfoFields.resolveKey("", "unknown", mockResolve))
        assertNull(DeviceInfoFields.resolveKey("", "110.7GB/264GB (8GB with storage extension)", mockResolve))
    }
}
