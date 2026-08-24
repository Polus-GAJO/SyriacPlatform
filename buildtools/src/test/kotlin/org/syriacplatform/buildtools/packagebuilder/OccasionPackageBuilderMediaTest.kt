package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader

class OccasionPackageBuilderMediaTest {

    private val builder =
        OccasionPackageBuilder()

    @Test
    fun mediaAwareBuildEmitsMetadataAndPhysicalMedia() {
        val mediaDirectory =
            Files.createTempDirectory(
                "syriacplatform-occasion-media-export-"
            )

        val mediaLibraryRoot =
            Files.createTempDirectory(
                "syriacplatform-media-library-"
            )

        val outputDirectory =
            testOutputDirectory()

        try {
            createMediaExportForAllRepresentativeMelodies(
                mediaDirectory
            )

            val sourceFile =
                mediaLibraryRoot
                    .resolve(
                        "audio/melodies/media-000001.mp3"
                    )

            Files.createDirectories(
                sourceFile.parent
            )

            sourceFile.writeBytes(
                byteArrayOf(
                    1, 2, 3, 4, 5
                )
            )

            val result =
                builder.build(
                    sourceDirectory =
                        representativeExportDirectory(),
                    mediaSourceDirectory =
                        mediaDirectory,
                    mediaLibraryRoot =
                        mediaLibraryRoot,
                    occasionId = 1L,
                    outputDirectory =
                        outputDirectory
                )

            assertTrue(
                result.packageData
                    .mediaAssets
                    .isNotEmpty()
            )

            result.packageData
                .melodies
                .forEach { melody ->
                    assertTrue(
                        melody.hasRecording
                    )

                    assertEquals(
                        listOf(1L),
                        melody.recordingIds
                    )
                }

            assertEquals(
                listOf(1L),
                result.packageData
                    .mediaAssets
                    .map {
                        it.id
                    }
            )

            assertEquals(
                "media/audio/melodies/media-000001.mp3",
                result.packageData
                    .mediaAssets
                    .single()
                    .path
            )

            val copiedFile =
                outputDirectory
                    .resolve(
                        "media/audio/melodies/media-000001.mp3"
                    )

            assertTrue(
                Files.isRegularFile(
                    copiedFile
                )
            )

            assertEquals(
                sourceFile.readText(),
                copiedFile.readText()
            )

            val mediaJson =
                outputDirectory
                    .resolve("content")
                    .resolve("media-assets.json")

            val melodiesJson =
                outputDirectory
                    .resolve("content")
                    .resolve("melodies.json")

            assertTrue(
                Files.isRegularFile(
                    mediaJson
                )
            )

            assertTrue(
                mediaJson
                    .readText()
                    .contains(
                        "\"path\": \"media/audio/melodies/media-000001.mp3\""
                    )
            )

            assertTrue(
                melodiesJson
                    .readText()
                    .contains(
                        "\"recordingIds\""
                    )
            )
        } finally {
            mediaDirectory
                .toFile()
                .deleteRecursively()

            mediaLibraryRoot
                .toFile()
                .deleteRecursively()
        }
    }

    private fun createMediaExportForAllRepresentativeMelodies(
        directory: Path
    ) {
        val source =
            AuthorSourceDataLoader()
                .load(
                    representativeExportDirectory()
                )

        directory
            .resolve("MediaAsset.csv")
            .writeText(
                """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/melodies/media-000001.mp3"
                """.trimIndent() + "\n"
            )

        val relations =
            buildString {
                appendLine(
                    "\"MelodyMediaID\",\"MelodyN\",\"MediaAssetID\",\"Role\",\"Sort\""
                )

                source.melodies
                    .forEachIndexed { index, melody ->
                        appendLine(
                            "\"${index + 1}\",\"${melody.id}\",\"1\",\"RECORDING\",\"1\""
                        )
                    }
            }

        directory
            .resolve("MelodyMedia.csv")
            .writeText(relations)
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
            "occasion-builder-media-test"
        )
            .toAbsolutePath()
            .normalize()
    }
}