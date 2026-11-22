import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions { jvmTarget = JvmTarget.JVM_11 }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.11.0-beta01")
    implementation(libs.compose.foundation)
    implementation(libs.media3.exoplayer)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "org.syriacplatform"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.syriacplatform.shhimo"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0-dev"
    }

    // Reuse the already tested Shhimo entry point while migrating its source.
    sourceSets {
        getByName("main") {
            java.srcDir("../androidApp/src/main/kotlin")
            java.exclude("**/MainActivity.kt")
        }
        getByName("debug") {
            assets.srcDir("../androidApp/src/debug/assets")
        }
    }

    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
