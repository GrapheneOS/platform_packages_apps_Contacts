import dev.detekt.gradle.Detekt

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.detekt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

detekt {
    basePath.set(rootDir)
    buildUponDefaultConfig = true
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    ignoredBuildTypes = listOf("release")
    parallel = true
    source.setFrom(files("../src"))
}

tasks.withType<Detekt>().configureEach {
    setSource(files("../src"))
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/build/**")
}

android {
    compileSdk = 36
    buildToolsVersion = "36.1.0"

    namespace = "com.android.contacts"

    buildFeatures {
        compose = true
        resValues = true
    }

    defaultConfig {
        minSdk = 36
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            val selfPkgName = android.namespace + applicationIdSuffix
            resValue("string", "applicationLabel", "Contacts d")
            resValue("string", "contacts_file_provider_authority", "$selfPkgName.files")
            resValue("string", "contacts_sdn_provider_authority", "$selfPkgName.sdn")
        }
    }

    sourceSets.getByName("main") {
        assets.directories.add("../assets")
        manifest.srcFile("../AndroidManifest.xml")
        java.directories.add("../src")
        java.directories.add("../src-bind")
        kotlin.directories.add("../src")
        res.directories.add("../res")
    }

    lint {
        abortOnError = false
        disable += setOf("UnusedResources", "UnusedIds")
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.coil.compose)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    implementation(libs.guava)

    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.material)

    implementation(project(":lib:platform_external_libphonenumber"))
    implementation(project(":lib:platform_frameworks_ex:common"))
    implementation(project(":lib:platform_frameworks_opt_vcard"))
    implementation(project(":lib:platform_packages_apps_PhoneCommon"))

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit4)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent)
    testImplementation(libs.mockk.android)
    testImplementation(libs.robolectric)
    testImplementation(libs.turbine)

    androidTestImplementation(kotlin("test"))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)

    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.agent)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.turbine)
}
