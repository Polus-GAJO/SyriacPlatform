import java.util.Properties
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

val developmentContentConfigFile =
    rootProject.projectDir
        .parentFile
        .resolve(
            "development-content.properties"
        )

val developmentContentProperties =
    Properties().apply {
        if (developmentContentConfigFile.isFile) {
            developmentContentConfigFile
                .inputStream()
                .use(::load)
        }
    }

val commandLineOccasionId =
    providers.gradleProperty(
        "occasionId"
    )
        .orNull

val configuredOccasionId =
    commandLineOccasionId
        ?: developmentContentProperties
            .getProperty("occasionId")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

val androidAppRequested =
    gradle.startParameter.taskNames
        .any { requestedTask ->
            val normalizedTask =
                if (requestedTask.startsWith(":")) {
                    requestedTask
                } else {
                    ":$requestedTask"
                }

            normalizedTask == ":androidApp" ||
                    normalizedTask.startsWith(
                        ":androidApp:"
                    )
        }

val developmentContentEnabled =
    androidAppRequested ||
            commandLineOccasionId != null

if (
    androidAppRequested &&
    configuredOccasionId == null
) {
    throw GradleException(
        "Reference App development content is not configured. " +
                "Create development-content.properties in " +
                "${rootProject.projectDir.parentFile} and set " +
                "occasionId to a positive integer."
    )
}

if (developmentContentEnabled) {
    require(
        configuredOccasionId
            ?.toLongOrNull()
            ?.let { it > 0L } == true
    ) {
        "occasionId must be a positive integer."
    }
}

val effectiveOccasionId =
    configuredOccasionId ?: "1"

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

        val occasionPreviewDirectory =
            buildtoolsProject.layout
                .buildDirectory
                .dir(
                    "generated/occasion-" +
                            "$effectiveOccasionId-preview"
                )

        from(
            layout.projectDirectory.dir(
                "src/commonMain/composeResources"
            )
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
