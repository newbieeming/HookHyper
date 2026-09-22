plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.newbieeming.hookhyper.core.hook"
}

dependencies {
    implementation(project(":core:common"))
    compileOnly(libs.libxposed.api)
    testImplementation(libs.libxposed.api)
    testImplementation(libs.junit)
}
