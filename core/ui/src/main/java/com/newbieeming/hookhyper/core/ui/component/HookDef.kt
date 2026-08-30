package com.newbieeming.hookhyper.core.ui.component

/**
 * Hook 定义接口。各 feature 模块的枚举实现此接口，
 * 集中声明 [preferenceKey]、[category]、[order] 三个属性。
 */
interface HookDef {

    /** 控制此 Hook 开关的偏好键名 */
    val preferenceKey: String

    /** 所属 UI 分类 */
    val category: HookCategory

    /** 在分类内的显示顺序，值越小越靠前 */
    val order: Int
}
