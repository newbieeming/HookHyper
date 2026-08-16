import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * 约定插件：Android Application 模块（不含 Compose）
 * 用法：plugins { alias(libs.plugins.android.application) }
 *
 * AGP 9.x 在应用 com.android.application 时会内部注册 kotlin extension，
 * 但不经过标准 pluginManager，所以用 extensions.findByName 替代 hasPlugin 检测。
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        // 仅当 kotlin extension 尚未注册时才手动应用（AGP 9.x 可能已自动注册）
        if (extensions.findByName("kotlin") == null) {
            pluginManager.apply("org.jetbrains.kotlin.android")
        }
        extensions.configure<ApplicationExtension> {
            compileSdk = DevKitBuildConfig.COMPILE_SDK
            defaultConfig {
                minSdk = DevKitBuildConfig.MIN_SDK
                targetSdk = DevKitBuildConfig.TARGET_SDK
            }
            compileOptions {
                sourceCompatibility = DevKitBuildConfig.JAVA_VERSION
                targetCompatibility = DevKitBuildConfig.JAVA_VERSION
            }
        }
    }
}
