plugins {
    alias(libs.plugins.jvm.library)
}

dependencies {
    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet.ksp)
}
