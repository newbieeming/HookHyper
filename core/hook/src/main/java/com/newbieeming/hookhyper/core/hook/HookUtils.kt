package com.newbieeming.hookhyper.core.hook

import java.lang.reflect.Field
import java.lang.reflect.Method

/** 反射工具方法，供各 [SubHooker] 实现使用。 */
object HookUtils {

    /** 获取静态字段值。 */
    fun Class<*>.staticField(name: String): Any? = findField(name).get(null)

    /** 获取实例字段值。 */
    fun Any.field(name: String): Any? = javaClass.findField(name).get(this)

    /** 调用实例方法。 */
    fun Any.call(name: String, vararg arguments: Any?): Any? =
        javaClass.findMethod(name, arguments).invoke(this, *arguments)

    /** 向上遍历类继承链查找字段。 */
    fun Class<*>.findField(name: String): Field {
        var current: Class<*>? = this
        while (current != null) {
            runCatching { current.getDeclaredField(name) }.getOrNull()?.let {
                it.isAccessible = true
                return it
            }
            current = current.superclass
        }
        error("Field $name not found in $this")
    }

    /** 向上遍历类继承链查找方法。 */
    fun Class<*>.findMethod(name: String, arguments: Array<out Any?>): Method {
        var current: Class<*>? = this
        while (current != null) {
            current.declaredMethods.firstOrNull { method ->
                method.name == name && method.parameterCount == arguments.size
            }?.let {
                it.isAccessible = true
                return it
            }
            current = current.superclass
        }
        error("Method $name/${arguments.size} not found in $this")
    }
}
