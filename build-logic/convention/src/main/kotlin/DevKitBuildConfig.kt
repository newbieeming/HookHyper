import org.gradle.api.JavaVersion

/** 全局构建常量，统一在此处维护，方便一键升级 SDK 版本 */
object DevKitBuildConfig {
    const val COMPILE_SDK = 37
    const val MIN_SDK     = 24
    const val TARGET_SDK  = 36
    val JAVA_VERSION      = JavaVersion.VERSION_17
}
