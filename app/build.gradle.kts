plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    compileSdk = 36
    buildToolsVersion = "36.1.0"

    namespace = "com.android.contacts"

    buildFeatures {
        resValues = true
    }

    defaultConfig {
        minSdk = 36
        targetSdk = 36
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
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.guava)
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.material)
    ksp(libs.hilt.compiler)

    implementation(project(":lib:platform_external_libphonenumber"))
    implementation(project(":lib:platform_frameworks_ex:common"))
    implementation(project(":lib:platform_frameworks_opt_vcard"))
    implementation(project(":lib:platform_packages_apps_PhoneCommon"))

    testImplementation(libs.kotlinx.coroutines.test)
}
