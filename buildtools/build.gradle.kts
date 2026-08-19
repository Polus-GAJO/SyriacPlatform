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

val occasionId =
    providers.gradleProperty("occasionId")
        .orElse("1")
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

tasks.register<JavaExec>(
    "buildOccasionPreview"
) {
    group = "syriacplatform"

    description =
        "Builds Occasion $occasionId export " +
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

    outputs.dir(
        occasionPreviewDirectory
    )

    args(
        occasionId,
        occasionSourceDirectory
            .asFile
            .absolutePath,
        occasionPreviewDirectory
            .get()
            .asFile
            .absolutePath
    )
}

tasks.register<Sync>(
    "syncDevelopmentPreviewToReferenceApp"
) {
    group = "syriacplatform"

    description =
        "Builds and copies Occasion $occasionId preview " +
                "into the Reference Application package resources."

    dependsOn(
        "buildOccasionPreview"
    )

    val referencePackageDirectory =
        layout.projectDirectory.dir(
            "../platform/shared/src/commonMain/" +
                    "composeResources/files"
        )

    from(
        occasionPreviewDirectory
    )

    into(
        referencePackageDirectory
    )
}

tasks.test {
    useJUnitPlatform()
}