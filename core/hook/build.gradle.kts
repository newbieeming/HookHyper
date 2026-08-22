plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.newbieeming.hookhyper.core.hook"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.yukihookapi.api)
}
