plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.android.hilt)
}

android {
    namespace = "com.newbieeming.hookhyper.core.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.yukihookapi.api)
    testImplementation(libs.junit)
}
