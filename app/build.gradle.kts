import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application.compose)
    alias(libs.plugins.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.newbieeming.hookhyper"

    defaultConfig {
        applicationId = "com.newbieeming.hookhyper"
        versionCode = 1_000_002
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("me") {
            storeFile = file("../sign/newbieeming.jks")
            storePassword = "newbieeming"
            keyAlias = "newbieeming"
            keyPassword = "newbieeming"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("me")
            isMinifyEnabled = true
            isShrinkResources = true
            optimization {
                enable = true
            }
        }
        debug {
            signingConfig = signingConfigs.findByName("me")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    androidResources.additionalParameters += listOf(
        "--allow-reserved-package-id",
        "--package-id",
        "0x64"
    )
}

androidComponents {
    onVariants { variant ->
        variant.manifestPlaceholders.put("BuildTime", buildTime)
        variant.manifestPlaceholders.put("GitCommitId", gitCommitId)

        if (variant.buildType == "release") {
            variant.outputs.forEach { output ->
                output.outputFileName.set(
                    variant.applicationId.zip(output.versionName) { applicationId, versionName ->
                        "$applicationId-$versionName.apk"
                    },
                )
            }
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":feature:systemui"))
    implementation(project(":feature:settings"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.miuix.ui.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.yukihookapi.api)
    compileOnly(libs.xposed.api)
    ksp(libs.yukihookapi.ksp.xposed)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}


val buildTime = providers.provider {
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX")
        .withZone(ZoneId.of("Asia/Shanghai"))
        .format(Instant.now())
}
val gitCommitId = providers.exec {
    commandLine("git", "rev-parse", "--short=8", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText.map { output ->
    output.trim().ifEmpty { "unknown" }
}
