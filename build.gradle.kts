import com.android.build.api.dsl.LibraryExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ktlint)
}

buildscript {
    dependencies {
        classpath(libs.hilt.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.ksp.gradle.plugin)
    }
}

val ktlintCliVersion: String =
    the<VersionCatalogsExtension>()
        .named("libs")
        .findVersion("ktlint")
        .get()
        .requiredVersion

configure<KtlintExtension> {
    version.set(ktlintCliVersion)

    filter {
        exclude("**/build/**")
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    plugins.withId("com.android.library") {
        // Without this, external (AOSP) libs are getting minSdk = 1 during build
        extensions.configure<LibraryExtension>("android") {
            defaultConfig {
                minSdk = 35
            }
        }

        // Without this, linter fails for the external (AOSP) libs
        if (path.startsWith(":lib:")) {
            tasks.configureEach {
                if (name.startsWith("lint")) {
                    enabled = false
                }
            }
        }
    }

    configure<KtlintExtension> {
        version.set(ktlintCliVersion)

        filter {
            exclude("**/build/**")
        }
    }
}
