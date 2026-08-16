plugins {
    alias(libs.plugins.android.library.compose)
    alias(libs.plugins.android.hilt)
}

android {
    namespace = "com.newbieeming.hookhyper.feature.systemui"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.yukihookapi.api)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)
    testImplementation(libs.junit)
}
