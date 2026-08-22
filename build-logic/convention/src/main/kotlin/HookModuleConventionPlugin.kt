import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * 约定插件：Hook 模块化
 *
 * 为 feature 模块提供：
 *   - :core:hook 依赖（@HookModule 注解 + SubHooker 接口）
 *   - KSP 处理器（自动生成 HookRegistry + 主 Hooker + Registrar）
 *
 * 用法：plugins { alias(libs.plugins.hook.module) }
 */
class HookModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.google.devtools.ksp")
        }
        dependencies {
            "implementation"(project(":core:hook"))
            "ksp"(project(":core:hook-ksp-processor"))
        }
    }
}
