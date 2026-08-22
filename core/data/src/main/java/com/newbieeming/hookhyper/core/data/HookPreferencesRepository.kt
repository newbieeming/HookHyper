package com.newbieeming.hookhyper.core.data

import android.content.Context
import com.highcapable.yukihookapi.hook.factory.prefs
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import com.newbieeming.hookhyper.core.common.UiStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HookPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private fun bridge() = context.prefs(PreferenceKeys.FILE_NAME)

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        bridge().getBoolean(key, defaultValue)

    fun putBoolean(key: String, value: Boolean) {
        bridge().edit { putBoolean(key, value) }
    }

    fun getString(key: String, defaultValue: String = ""): String =
        bridge().getString(key, defaultValue)

    fun putString(key: String, value: String) {
        bridge().edit { putString(key, value) }
    }

    fun getUiStyle(): UiStyle = runCatching {
        UiStyle.valueOf(getString(PreferenceKeys.UI_STYLE, UiStyle.MATERIAL.name))
    }.getOrDefault(UiStyle.MATERIAL)

    fun setUiStyle(style: UiStyle) = putString(PreferenceKeys.UI_STYLE, style.name)
}
