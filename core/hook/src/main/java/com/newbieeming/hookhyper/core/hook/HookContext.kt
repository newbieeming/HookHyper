package com.newbieeming.hookhyper.core.hook

import android.content.SharedPreferences
import io.github.libxposed.api.XposedInterface

/** Package context created after the target classloader is ready. */
class HookContext(
    val xposed: XposedInterface,
    val packageName: String,
    val classLoader: ClassLoader,
) {
    fun String.toClass(): Class<*> = Class.forName(this, false, classLoader)

    fun prefs(name: String): HookPreferences = HookPreferences(xposed.getRemotePreferences(name))
}

class HookPreferences(private val preferences: SharedPreferences) {
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = preferences.getBoolean(key, defaultValue)

    fun getString(key: String, defaultValue: String = ""): String = preferences.getString(key, defaultValue) ?: defaultValue
}
