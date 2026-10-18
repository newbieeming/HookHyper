package com.newbieeming.hookhyper.core.hook

/**
 * 标记一个 [SubHooker] 实现类，KSP 处理器会在编译期扫描此注解并自动生成注册表.
 *
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class HookModule(
    /** 目标应用包名，如 com.android.systemui. */
    val packageName: String,
)
