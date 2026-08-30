package com.newbieeming.hookhyper.core.ui.component

/**
 * Hook 分类接口。各 feature 模块定义自己的枚举实现此接口。
 */
interface HookCategory {

    /** 分类的唯一标识 */
    val id: String

    /** 分类显示顺序，值越小越靠前 */
    val order: Int

    /** 分类标题的 string resource id */
    val titleResId: Int
}
