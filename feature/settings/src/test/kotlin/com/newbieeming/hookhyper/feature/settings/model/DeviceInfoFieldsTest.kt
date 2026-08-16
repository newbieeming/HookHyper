package com.newbieeming.hookhyper.feature.settings.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeviceInfoFieldsTest {
    @Test
    fun `recognizes Chinese values shown by HyperOS device cards`() {
        assertEquals("settings_device_memory", DeviceInfoFields.preferenceKeyForValue("12 + 4 GB"))
        assertEquals("settings_device_battery", DeviceInfoFields.preferenceKeyForValue("5000 mAh"))
        assertEquals("settings_device_screen_resolution", DeviceInfoFields.preferenceKeyForValue("3200 x 1440"))
        assertEquals("settings_device_cpu", DeviceInfoFields.preferenceKeyForValue("骁龙 8 至尊版"))
        assertEquals("settings_device_baseband", DeviceInfoFields.preferenceKeyForValue("MPSS.DE.4.0"))
        assertEquals("settings_device_name", DeviceInfoFields.preferenceKeyForValue("Xiaomi 15 Ultra"))
        assertNull(DeviceInfoFields.preferenceKeyForValue("unknown"))
    }

    @Test
    fun `recognizes English titles shown by HyperOS device cards`() {
        assertEquals("settings_device_name", DeviceInfoFields.preferenceKeyForTitle("Device name"))
        assertEquals("settings_device_cpu", DeviceInfoFields.preferenceKeyForTitle("CPU"))
        assertEquals("settings_device_memory", DeviceInfoFields.preferenceKeyForTitle("RAM"))
        assertEquals("settings_device_battery", DeviceInfoFields.preferenceKeyForTitle("Battery capacity"))
        assertEquals("settings_device_screen_size", DeviceInfoFields.preferenceKeyForTitle("Screen size"))
        assertEquals("settings_device_screen_resolution", DeviceInfoFields.preferenceKeyForTitle("Resolution"))
        assertEquals("settings_device_os_version", DeviceInfoFields.preferenceKeyForTitle("OS version"))
        assertEquals("settings_device_camera", DeviceInfoFields.preferenceKeyForTitle("Camera"))
        assertEquals("settings_device_baseband", DeviceInfoFields.preferenceKeyForTitle("Baseband version"))
    }

    @Test
    fun `recognizes English values shown by HyperOS device cards`() {
        assertEquals(
            "settings_device_cpu",
            DeviceInfoFields.preferenceKeyForValue("Snapdragon® 8 Elite Gen 5 Mobile Platform"),
        )
        assertEquals("settings_device_memory", DeviceInfoFields.preferenceKeyForValue("12.0+4.0 GB"))
        assertEquals("settings_device_battery", DeviceInfoFields.preferenceKeyForValue("7560mAh(typ)"))
        assertEquals("settings_device_screen_size", DeviceInfoFields.preferenceKeyForValue("6.9”"))
        assertEquals(
            "settings_device_screen_resolution",
            DeviceInfoFields.preferenceKeyForValue("2608 x 1200 Full RGB"),
        )
        assertEquals(
            "settings_device_camera",
            DeviceInfoFields.preferenceKeyForValue("Front32MP | Rear50MP+50MP+50MP"),
        )
        assertEquals(
            "settings_device_os_version",
            DeviceInfoFields.preferenceKeyForValue("4.0.0.15.XPMCNXM.D00"),
        )
    }

    @Test
    fun `does not confuse storage with RAM`() {
        assertNull(
            DeviceInfoFields.preferenceKeyForValue(
                "110.7GB/264GB (8GB with storage extension)",
            ),
        )
    }
}
