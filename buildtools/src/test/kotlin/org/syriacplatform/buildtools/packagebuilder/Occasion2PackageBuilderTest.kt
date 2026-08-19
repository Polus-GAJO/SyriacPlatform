package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Occasion2PackageBuilderTest {

    private val builder =
        OccasionPackageBuilder()

    @Test
    fun buildsRealOccasion2ExportThroughUnifiedBuilder() {

        val sourceDirectory =
            occasion2ExportDirectory()

        val outputDirectory =
            occasion2OutputDirectory()

        assertTrue(
            Files.isDirectory(
                sourceDirectory
            ),
            "Occasion 2 export directory does not exist: " +
                    sourceDirectory
        )

        val result =
            builder.build(
                sourceDirectory =
                    sourceDirectory,
                occasionId = 2L,
                outputDirectory =
                    outputDirectory
            )

        assertEquals(
            2L,
            result.occasionId
        )

        assertEquals(
            outputDirectory
                .toAbsolutePath()
                .normalize(),
            result.outputDirectory
        )

        assertEquals(
            "org.syriacplatform.preview.occasion2",
            result.packageData
                .manifest
                .packageId
        )

        assertEquals(
            "Occasion 2 Development Preview",
            result.packageData
                .manifest
                .packageName
        )

        assertEquals(
            "occasion-2-preview",
            result.packageData
                .manifest
                .contentVersion
        )

        assertEquals(
            2L,
            result.packageData
                .occasions
                .single()
                .id
        )

        assertTrue(
            result.prayerCount > 0,
            "Occasion 2 produced no prayers."
        )

        assertTrue(
            result.liturgicalItemCount > 0,
            "Occasion 2 produced no liturgical items."
        )

        assertTrue(
            Files.isRegularFile(
                outputDirectory
                    .resolve(
                        "manifest.json"
                    )
            )
        )

        assertTrue(
            Files.isRegularFile(
                outputDirectory
                    .resolve("content")
                    .resolve(
                        "occasions.json"
                    )
            )
        )

        assertTrue(
            Files.isRegularFile(
                outputDirectory
                    .resolve("content")
                    .resolve(
                        "prayers.json"
                    )
            )
        )

        assertTrue(
            Files.isRegularFile(
                outputDirectory
                    .resolve("content")
                    .resolve(
                        "prayer-sequences.json"
                    )
            )
        )

        assertTrue(
            Files.isRegularFile(
                outputDirectory
                    .resolve("content")
                    .resolve(
                        "liturgical-items.json"
                    )
            )
        )

        println(
            "Occasion 2 package built successfully."
        )

        println(
            "Source: $sourceDirectory"
        )

        println(
            "Output: $outputDirectory"
        )

        println(
            "Prayers: ${result.prayerCount}"
        )

        println(
            "Liturgical items: " +
                    result.liturgicalItemCount
        )
    }

    private fun occasion2ExportDirectory():
            Path {

        return Path.of(
            "..",
            "author-database",
            "exports",
            "occasion-2"
        )
            .toAbsolutePath()
            .normalize()
    }

    private fun occasion2OutputDirectory():
            Path {

        return Path.of(
            "..",
            "buildtools",
            "build",
            "generated",
            "occasion-2-preview"
        )
            .toAbsolutePath()
            .normalize()
    }
}