import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.gradle.kotlin.dsl.configure

/**
 * 约定插件：纯 JVM 库（:core:model 使用）
 * jvmToolchain 同时对齐 compileJava 和 compileKotlin，避免 JVM target 不一致告警。
 * 用法：plugins { alias(libs.plugins.jvm.library) }
 */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        // jvmToolchain 同时设置 compileJava 和 compileKotlin 的目标版本，
        // 解决 "Inconsistent JVM Target Compatibility" 问题
        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(DevKitBuildConfig.JAVA_VERSION.majorVersion.toInt())
        }
    }
}
