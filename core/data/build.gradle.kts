plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.android.hilt)
}

android {
    namespace = "com.newbieeming.hookhyper.core.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    api(libs.libxposed.service)
    testImplementation(libs.junit)
}
