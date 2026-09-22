package com.newbieeming.hookhyper.hook

import com.newbieeming.hookhyper.core.hook.HookContext
import com.newbieeming.hookhyper.hook.gen.GeneratedHookEntry
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

class HookEntry : XposedModule() {
    override fun onPackageReady(param: PackageReadyParam) {
        GeneratedHookEntry.register(HookContext(this, param.packageName, param.classLoader))
    }
}
