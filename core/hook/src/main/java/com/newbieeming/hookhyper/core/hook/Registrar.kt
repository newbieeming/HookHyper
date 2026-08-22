package com.newbieeming.hookhyper.core.hook

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

/**
 * 由 KSP 为每个 feature 模块自动生成，供 [ServiceLoader] 自动发现。
 * 应用层通过 [ServiceLoader.load] 扫描所有实现，无需手动注册。
 */
interface Registrar {
    fun hooker(): YukiBaseHooker
}
