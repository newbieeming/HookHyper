package com.newbieeming.hookhyper.core.hook


/**
 * 由 KSP 为每个 feature 模块自动生成，供 KSP 生成的入口 自动发现。
 * 应用层通过 KSP 生成的入口 扫描所有实现，无需手动注册。
 */
interface Registrar {
    fun hooker(): ModularHooker
}
