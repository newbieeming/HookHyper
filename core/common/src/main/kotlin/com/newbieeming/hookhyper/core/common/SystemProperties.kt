package com.newbieeming.hookhyper.core.common

/**
 * 读取 Android System Properties 的工具类。
 *
 * 由于 [android.os.SystemProperties] 是 @hide API，
 * 通过反射调用以避免编译时依赖。
 */
object SystemProperties {

    private val GET_METHOD by lazy {
        try {
            Class.forName("android.os.SystemProperties")
                .getMethod("get", String::class.java, String::class.java)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 读取系统属性。
     *
     * @param key 属性名
     * @param default 默认值，属性不存在或反射失败时返回
     * @return 属性值，若不存在则返回 [default]
     */
    @JvmStatic
    fun get(key: String, default: String = ""): String = try {
        GET_METHOD?.invoke(null, key, default) as? String ?: default
    } catch (_: Exception) {
        default
    }
}
