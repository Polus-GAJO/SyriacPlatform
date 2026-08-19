plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    testImplementation(libs.kotlin.test)
}

tasks.register<Sync>(
    "syncDevelopmentPreviewToReferenceApp"
) {
    group = "syriacplatform"

    val occasionId =
        providers.gradleProperty(
            "occasionId"
        )
            .orElse("1")

    description =
        "Copies the selected Occasion development preview " +
                "into the Reference Application package resources."

    dependsOn("test")

    val previewDirectory =
        layout.buildDirectory.dir(
            occasionId.map { id ->
                "generated/occasion-$id-preview"
            }
        )

    val referencePackageDirectory =
        layout.projectDirectory.dir(
            "../platform/shared/src/commonMain/composeResources/files"
        )

    from(previewDirectory)

    into(referencePackageDirectory)

    doFirst {
        val id =
            occasionId.get()

        require(
            id.toLongOrNull()?.let { it > 0L } == true
        ) {
            "occasionId must be a positive integer."
        }

        val source =
            previewDirectory
                .get()
                .asFile

        require(
            source.isDirectory
        ) {
            "Development preview package for Occasion " +
                    "$id was not generated: $source"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}