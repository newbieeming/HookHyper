package com.newbieeming.hookhyper.core.hook

import com.highcapable.yukihookapi.hook.param.PackageParam

/**
 * 子模块 Hook 接口。
 *
 * 每个功能拆分为一个独立实现类，配合 [@HookModule] 注解声明元数据。
 * 主 Hooker 通过 KSP 生成的注册表遍历并执行各子模块。
 */
interface SubHooker {

    /** 控制此模块开关的偏好键名。 */
    val preferenceKey: String

    /** 在目标应用的 [PackageParam] 上下文中执行 Hook 逻辑。 */
    fun PackageParam.onHook()
}
