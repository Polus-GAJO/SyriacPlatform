package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OccasionPackageBuilderTest {

    private val builder =
        OccasionPackageBuilder()

    @Test
    fun buildsRepresentativeOccasionThroughUnifiedBuilder() {

        val outputDirectory =
            testOutputDirectory()

        val result =
            builder.build(
                sourceDirectory =
                    representativeExportDirectory(),
                occasionId = 1L,
                outputDirectory =
                    outputDirectory
            )

        assertEquals(
            1L,
            result.occasionId
        )

        assertEquals(
            outputDirectory
                .toAbsolutePath()
                .normalize(),
            result.outputDirectory
        )

        assertEquals(
            52,
            result.liturgicalItemCount
        )

        assertTrue(
            result.prayerCount > 0
        )

        assertEquals(
            "org.syriacplatform.preview.occasion1",
            result.packageData
                .manifest
                .packageId
        )

        assertEquals(
            "Occasion 1 Development Preview",
            result.packageData
                .manifest
                .packageName
        )

        assertEquals(
            "occasion-1-preview",
            result.packageData
                .manifest
                .contentVersion
        )

        assertTrue(
            Files.isRegularFile(
                result.outputDirectory
                    .resolve("manifest.json")
            )
        )

        assertTrue(
            Files.isRegularFile(
                result.outputDirectory
                    .resolve("content")
                    .resolve(
                        "liturgical-items.json"
                    )
            )
        )

        assertTrue(
            Files.isRegularFile(
                result.outputDirectory
                    .resolve("content")
                    .resolve(
                        "prayer-sequences.json"
                    )
            )
        )
    }

    @Test
    fun rejectsRequestedOccasionThatDoesNotMatchSource() {

        val exception =
            kotlin.test.assertFailsWith<
                    IllegalArgumentException
                    > {
                builder.build(
                    sourceDirectory =
                        representativeExportDirectory(),
                    occasionId = 2L,
                    outputDirectory =
                        testOutputDirectory()
                )
            }

        assertTrue(
            exception
                .message
                .orEmpty()
                .contains(
                    "does not match source Occasion"
                )
        )
    }

    private fun representativeExportDirectory():
            Path {

        return Path.of(
            "..",
            "author-database",
            "samples",
            "mapping-analysis"
        )
            .toAbsolutePath()
            .normalize()
    }

    private fun testOutputDirectory():
            Path {

        return Path.of(
            "..",
            "buildtools",
            "build",
            "generated",
            "occasion-builder-test"
        )
            .toAbsolutePath()
            .normalize()
    }
}