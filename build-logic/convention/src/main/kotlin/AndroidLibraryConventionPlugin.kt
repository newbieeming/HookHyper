import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * 约定插件：Android Library 模块（不含 Compose）
 * 用法：plugins { alias(libs.plugins.android.library) }
 *
 * AGP 9.x 提供内置 Kotlin 支持，因此不再应用 org.jetbrains.kotlin.android。
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
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
