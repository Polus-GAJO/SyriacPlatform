import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    
    androidLibrary {
       namespace = "org.syriacplatform.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)

        }
        commonTest.dependencies {
           implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

val buildtoolsProject =
    project(":buildtools")

val developmentComposeResourcesDirectory =
    layout.buildDirectory.dir(
        "generated/developmentComposeResources"
    )

val prepareDevelopmentContentResources =
    tasks.register<Sync>(
        "prepareDevelopmentContentResources"
    ) {
        group = "syriacplatform"

        description =
            "Prepares the generated development content package " +
                    "as Compose Multiplatform resources."

        dependsOn(
            buildtoolsProject.tasks.named(
                "buildOccasionPreview"
            )
        )

        val occasionId =
            providers.gradleProperty(
                "occasionId"
            )
                .orElse("1")

        val occasionPreviewDirectory =
            buildtoolsProject.layout
                .buildDirectory
                .dir(
                    occasionId.map { id ->
                        "generated/occasion-$id-preview"
                    }
                )

        from(
            occasionPreviewDirectory
        ) {
            into("files")
        }

        into(
            developmentComposeResourcesDirectory
        )
    }

val developmentContentEnabled =
    providers.gradleProperty(
        "occasionId"
    )
        .isPresent

compose.resources {
    packageOfResClass = "org.syriacplatform.resources"
    generateResClass = always

    if (developmentContentEnabled) {
        customDirectory(
            sourceSetName = "commonMain",
            directoryProvider =
                layout.dir(
                    prepareDevelopmentContentResources.map {
                        it.destinationDir
                    }
                )
        )
    }
}
