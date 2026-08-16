import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * 约定插件：Android Library 模块（不含 Compose）
 * 用法：plugins { alias(libs.plugins.android.library) }
 *
 * AGP 9.x 在应用 com.android.library 时可能内部注册 kotlin extension，
 * 用 extensions.findByName 替代 hasPlugin 检测，避免重复注册。
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        if (extensions.findByName("kotlin") == null) {
            pluginManager.apply("org.jetbrains.kotlin.android")
        }
        extensions.configure<LibraryExtension> {
            compileSdk = DevKitBuildConfig.COMPILE_SDK
            defaultConfig.minSdk = DevKitBuildConfig.MIN_SDK
            compileOptions {
                sourceCompatibility = DevKitBuildConfig.JAVA_VERSION
                targetCompatibility = DevKitBuildConfig.JAVA_VERSION
            }
        }
    }
}
