import com.diffplug.gradle.spotless.SpotlessExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.agp.application) apply false
    alias(libs.plugins.agp.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.spotless) apply false
}

subprojects {
    apply(plugin = "dev.detekt")
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<dev.detekt.gradle.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = true
        basePath.set(rootProject.layout.projectDirectory)
        config.setFrom(rootProject.files("config/detekt/detekt-config.yml"))
    }

    tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
        reports {
            checkstyle.required.set(true)
            html.required.set(true)
            markdown.required.set(true)
        }
    }

    extensions.configure<SpotlessExtension> {
        java {
            target("src/**/*.java")
            removeUnusedImports()
            formatAnnotations()
        }

        kotlin {
            target("src/**/*.kt")
            ktlint()
                .editorConfigOverride(
                    mapOf(
                        "android" to true,
                        "ktlint_standard_function-naming" to "disabled",
                        "ktlint_standard_property-naming" to "disabled",
                    ),
                )
        }

        kotlinGradle {
            target("**/*.gradle.kts")
            ktlint().editorConfigOverride(mapOf("android" to true))
        }

        format("xml") {
            target("src/**/*.xml")
        }
    }
}
