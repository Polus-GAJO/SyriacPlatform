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

tasks.register<Sync>("syncDevelopmentPreviewToReferenceApp") {
    group = "syriacplatform"

    description =
        "Copies the generated Occasion 1 development preview " +
                "into the Reference Application package resources."

    dependsOn("test")

    val previewDirectory =
        layout.buildDirectory.dir(
            "generated/occasion-1-preview"
        )

    val referencePackageDirectory =
        layout.projectDirectory.dir(
            "../platform/shared/src/commonMain/composeResources/files"
        )

    from(previewDirectory)

    into(referencePackageDirectory)

    doFirst {
        val source =
            previewDirectory.get().asFile

        require(source.isDirectory) {
            "Development preview package was not generated: $source"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}