import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * 约定插件：Hilt 依赖注入
 *
 * 应用 Hilt Gradle 插件（字节码变换）+ KSP 注解处理器。
 * 所有使用此插件的模块自动获得：
 *   - hilt-android 运行时（注解可见）
 *   - hilt-android-compiler KSP 处理器（代码生成）
 *   - error-prone-annotations：Hilt 2.60+ 生成的 Java 代码中引用了
 *     @CanIgnoreReturnValue，需要显式声明才能通过 javac 编译
 *
 * 用法：plugins { alias(libs.plugins.android.hilt) }
 */
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.google.dagger.hilt.android")
            apply("com.google.devtools.ksp")
        }
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        dependencies {
            "implementation"(libs.findLibrary("hilt-android").get())
            "ksp"(libs.findLibrary("hilt-android-compiler").get())
            // Hilt 2.60 生成的代码依赖 @CanIgnoreReturnValue，需显式加入编译路径
            "compileOnly"(libs.findLibrary("errorprone-annotations").get())
        }
    }
}
