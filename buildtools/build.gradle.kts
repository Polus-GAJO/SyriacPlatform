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

tasks.register<JavaExec>("buildBookPrayerPreview") {
    group = "syriacplatform"
    description = "Builds a book/day/prayer Schema-v1 development preview from CSV."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.syriacplatform.buildtools.packagebuilder.BookPrayerPreviewBuildMainKt")
    val bookId = providers.gradleProperty("bookId")
    val dayId = providers.gradleProperty("dayId")
    val prayerId = providers.gradleProperty("prayerId")
    val sourceDir = providers.gradleProperty("sourceDir")
    val outputDir = providers.gradleProperty("outputDir")
    doFirst {
        args(
            bookId.get(), dayId.get(), prayerId.get(),
            sourceDir.get(), outputDir.get()
        )
    }
}

tasks.register<JavaExec>("inspectShhimoDailyNavigation") {
    group = "syriacplatform"
    description = "Lists Shhimo daily prayers from exported ExistsIn.csv (excludes DayN=0)."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.syriacplatform.buildtools.schema.ShhimoDailyNavigationMainKt")
    val bookId = providers.gradleProperty("bookId")
    val sourceDir = providers.gradleProperty("sourceDir")
    doFirst { args(bookId.get(), sourceDir.get()) }
}

tasks.register<JavaExec>("validateShhimoDailySequences") {
    group = "syriacplatform"
    description = "Checks all daily Shhimo prayer sequence IDs, items and verse ordering."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.syriacplatform.buildtools.schema.ShhimoDailySequenceValidationMainKt")
    val bookId = providers.gradleProperty("bookId")
    val sourceDir = providers.gradleProperty("sourceDir")
    doFirst { args(bookId.get(), sourceDir.get()) }
}

tasks.register<JavaExec>("buildShhimoDailyPackage") {
    group = "syriacplatform"
    description = "Builds the complete seven-day Shhimo Book-profile package."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.syriacplatform.buildtools.packagebuilder.ShhimoDailyPackageBuildMainKt")
    val bookId = providers.gradleProperty("bookId")
    val sourceDir = providers.gradleProperty("sourceDir")
    val outputDir = providers.gradleProperty("outputDir")
    val includeMedia = providers.gradleProperty("includeMedia").map(String::toBoolean).orElse(false)
    val mediaCsvPath = mediaSourceDirectory.asFile.absolutePath
    val mediaRootPath = file(mediaLibraryRoot).absolutePath
    if (includeMedia.get()) {
        args(bookId.get(), sourceDir.get(), mediaCsvPath, mediaRootPath, outputDir.get())
    } else {
        args(bookId.get(), sourceDir.get(), outputDir.get())
    }
}
