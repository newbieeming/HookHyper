package com.newbieeming.hookhyper.hook

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.newbieeming.hookhyper.BuildConfig
import com.newbieeming.hookhyper.hook.gen.GeneratedHookEntry

@InjectYukiHookWithXposed(
    isUsingXposedModuleStatus = true,
)
object HookEntry : IYukiHookXposedInit {
    override fun onInit() = YukiHookAPI.configs {
        isDebug = BuildConfig.DEBUG
        debugLog {
            tag = "HookHyper"
            isEnable = BuildConfig.DEBUG
        }
    }

    override fun onHook() = GeneratedHookEntry.register()
}
