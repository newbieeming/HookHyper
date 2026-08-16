import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * 约定插件：Android Application + Compose
 * 在 AndroidApplicationConventionPlugin 基础上启用 Compose 编译器。
 * 用法：plugins { alias(libs.plugins.android.application.compose) }
 */
class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("hookhyper.android.application")
            apply("org.jetbrains.kotlin.plugin.compose")
        }
        extensions.configure<ApplicationExtension> {
            buildFeatures { compose = true }
        }
    }
}
