import java.util.Properties

plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(
        "org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0"
    )

    testImplementation(
        libs.kotlin.test
    )
}

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

val localOccasionId =
    developmentContentProperties
        .getProperty("occasionId")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

val occasionId =
    providers.gradleProperty("occasionId")
        .orElse(localOccasionId ?: "1")
        .get()

require(
    occasionId.toLongOrNull()
        ?.let { it > 0L } == true
) {
    "occasionId must be a positive integer."
}
val occasionSourceDirectory =
    layout.projectDirectory.dir(
        "../author-database/exports/occasion-$occasionId"
    )

val occasionPreviewDirectory =
    layout.buildDirectory.dir(
        "generated/occasion-$occasionId-preview"
    )

val mediaSourceDirectory =
    layout.projectDirectory.dir(
        "../author-database/exports/media"
    )

val localMediaLibraryRoot =
    developmentContentProperties
        .getProperty(
            "mediaLibraryRoot"
        )
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

val mediaLibraryRoot =
    providers.gradleProperty(
        "mediaLibraryRoot"
    )
        .orElse(
            providers.environmentVariable(
                "SYRIACPLATFORM_MEDIA_ROOT"
            )
        )
        .orElse(
            localMediaLibraryRoot
                ?: "D:\\SyriacPlatformMedia"
        )
        .get()
tasks.register<JavaExec>(
    "buildOccasionPreview"
) {
    group = "syriacplatform"

    description =
        "Builds Occasion $occasionId export with media " +
                "into a Schema-v1 preview package."

    classpath =
        sourceSets["main"]
            .runtimeClasspath

    mainClass.set(
        "org.syriacplatform.buildtools.packagebuilder." +
                "OccasionPackageBuildMainKt"
    )

    inputs.dir(
        occasionSourceDirectory
    )

    inputs.dir(
        mediaSourceDirectory
    )

    inputs.dir(
        file(mediaLibraryRoot)
    )

    outputs.dir(
        occasionPreviewDirectory
    )

    args(
        occasionId,
        occasionSourceDirectory
            .asFile
            .absolutePath,
        mediaSourceDirectory
            .asFile
            .absolutePath,
        file(
            mediaLibraryRoot
        )
            .absolutePath,
        occasionPreviewDirectory
            .get()
            .asFile
            .absolutePath
    )
}

tasks.test {
    useJUnitPlatform()
}
