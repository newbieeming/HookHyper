plugins {
    alias(libs.plugins.android.library.compose)
}

android {
    namespace = "com.newbieeming.hookhyper.core.ui"
}

dependencies {
    api(project(":core:common"))
    implementation(project(":core:data"))
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material.icons)
    api(libs.miuix.ui.android)
    api(libs.miuix.preference.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.kotlinx.coroutines.android)
}
