import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * 约定插件：Android Library + Compose
 * 所有需要 Compose UI 的 :core:ui、:core:designsystem、:feature:* 模块使用。
 * 用法：plugins { alias(libs.plugins.android.library.compose) }
 */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("hookhyper.android.library")
            apply("org.jetbrains.kotlin.plugin.compose")
        }
        extensions.configure<LibraryExtension> {
            buildFeatures { compose = true }
        }
    }
}
