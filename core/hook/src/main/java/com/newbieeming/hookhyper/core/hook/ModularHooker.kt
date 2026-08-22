package com.newbieeming.hookhyper.core.hook

import android.util.Log
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.newbieeming.hookhyper.core.common.PreferenceKeys

/**
 * 模块化 Hooker 基类。
 *
 * 自动通过反射查找同包下 KSP 生成的 [HookRegistry]，遍历子模块并按偏好键判断开关后执行。
 * 子类只需提供 [tag] 和 [targetPackage]。
 *
 * @param tag 日志标签，如 `"HookHyper-SystemUI"`
 * @param targetPackage 目标应用包名，如 `"com.android.systemui"`
 */
abstract class ModularHooker(
    private val tag: String,
    private val targetPackage: String,
) : YukiBaseHooker() {

    override fun onHook() {
        loadApp(name = targetPackage) {
            val featurePreferences = prefs(PreferenceKeys.FILE_NAME)
            for ((key, factory) in modules()) {
                if (!featurePreferences.getBoolean(key)) continue
                with(factory()) {
                    runCatching { this@loadApp.onHook() }
                        .onFailure { Log.e(tag, "Hook failed: $key", it) }
                }
            }
        }
    }

    companion object {
        private const val REGISTRY_CLASS = "HookRegistry"
        private const val REGISTRY_FIELD = "modules"

        /**
         * 通过反射获取当前子类同包下 KSP 生成的 HookRegistry.modules。
         */
        @Suppress("UNCHECKED_CAST")
        private fun Any.modules(): List<Pair<String, () -> SubHooker>> = runCatching {
            val pkg = javaClass.`package`?.name ?: return@runCatching emptyList()
            val clazz = Class.forName("$pkg.$REGISTRY_CLASS")
            val field = clazz.getDeclaredField(REGISTRY_FIELD).apply { isAccessible = true }
            field.get(null) as List<Pair<String, () -> SubHooker>>
        }.getOrElse {
            Log.e("ModularHooker", "HookRegistry not found for ${javaClass.name}", it)
            emptyList()
        }
    }
}
