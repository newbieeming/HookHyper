package com.newbieeming.hookhyper.core.ui.component

/**
 * Feature Hook 统一接口。
 *
 * 各 feature 模块的 hook 实现类只需声明 [def] 枚举条目，
 * [preferenceKey]、[category]、[order] 自动委托，无需重复声明。
 *
 * hook 类同时实现 [SubHooker][com.newbieeming.hookhyper.core.hook.SubHooker] 和此接口即可。
 *
 * @param T 对应的 [HookDef] 枚举类型
 */
interface FeatureHook<T : HookDef> :
    HookContent,
    HookDef {

    val def: T
    override val preferenceKey get() = def.preferenceKey
    override val category get() = def.category
    override val order get() = def.order
}
