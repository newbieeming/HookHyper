package com.newbieeming.hookhyper.core.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.newbieeming.hookhyper.core.common.PreferenceKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HookPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val local by lazy { context.getSharedPreferences(PreferenceKeys.FILE_NAME, Context.MODE_PRIVATE) }
    private var service: XposedService? = null
    private var remote: SharedPreferences? = null
    private var listening = false
    private val mutableStatus = MutableStateFlow(ModuleStatus(false, "", null))
    val status = mutableStatus.asStateFlow()

    /** Keep local settings usable without a framework, and publish them when the service binds. */
    @Synchronized
    fun connect() {
        if (listening) return
        listening = true
        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) = bind(service)

            override fun onServiceDied(service: XposedService) = disconnect(service)
        })
    }

    @Synchronized
    private fun bind(connection: XposedService) {
        runCatching {
            val preferences = connection.getRemotePreferences(PreferenceKeys.FILE_NAME)
            preferences.edit {
                local.all.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> putBoolean(key, value)
                        is String -> putString(key, value)
                    }
                }
            }
            remote = preferences
            service = connection
            mutableStatus.value = ModuleStatus(true, connection.frameworkName, connection.apiVersion)
        }.onFailure { Log.e("HookPreferences", "Unable to connect to Xposed preferences", it) }
    }

    @Synchronized
    private fun disconnect(connection: XposedService) {
        if (service !== connection) return
        remote = null
        service = null
        mutableStatus.value = ModuleStatus(false, "", null)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = local.getBoolean(key, defaultValue)

    @Synchronized
    fun putBoolean(key: String, value: Boolean) {
        local.edit { putBoolean(key, value) }
        publish { putBoolean(key, value) }
    }

    fun getString(key: String, defaultValue: String = ""): String = local.getString(key, defaultValue) ?: defaultValue

    @Synchronized
    fun putString(key: String, value: String) {
        local.edit { putString(key, value) }
        publish { putString(key, value) }
    }

    private fun publish(change: SharedPreferences.Editor.() -> Unit) {
        runCatching { remote?.edit(action = change) }
            .onFailure {
                Log.e("HookPreferences", "Unable to publish settings; retained locally", it)
                service?.let(::disconnect)
            }
    }
}
