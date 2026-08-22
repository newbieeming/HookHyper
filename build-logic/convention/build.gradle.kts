plugins {
    `kotlin-dsl`
}

group = "com.newbieeming.hookhyper.buildlogic"

// jvmToolchain 同时设置 compileJava 和 compileKotlin 的目标版本，
// 避免两者不一致触发 Gradle 告警
kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.plugins.agp.application.toDep())
    compileOnly(libs.plugins.agp.library.toDep())
    compileOnly(libs.plugins.kotlin.android.toDep())
    compileOnly(libs.plugins.kotlin.compose.toDep())
    compileOnly(libs.plugins.kotlin.jvm.toDep())
    compileOnly(libs.plugins.hilt.toDep())
    compileOnly(libs.plugins.ksp.toDep())
}

// 将 PluginDependency 转换为 ModuleDependency，方便 compileOnly 引入插件 jar
fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

gradlePlugin {
    plugins {
        // Android Application
        register("androidApplication") {
            id = "hookhyper.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        // Android Application + Compose
        register("androidApplicationCompose") {
            id = "hookhyper.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        // Android Library
        register("androidLibrary") {
            id = "hookhyper.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        // Android Library + Compose
        register("androidLibraryCompose") {
            id = "hookhyper.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        // Hilt DI
        register("androidHilt") {
            id = "hookhyper.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        // 纯 JVM 库（core:common 使用）
        register("jvmLibrary") {
            id = "hookhyper.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        // Hook 模块化（KSP 注解处理器 + core:hook 依赖）
        register("hookModule") {
            id = "hookhyper.hook.module"
            implementationClass = "HookModuleConventionPlugin"
        }
    }
}
