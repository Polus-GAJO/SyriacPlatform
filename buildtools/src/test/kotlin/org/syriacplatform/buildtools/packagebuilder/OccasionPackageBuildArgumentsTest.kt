package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OccasionPackageBuildArgumentsTest {

    @Test
    fun parsesLegacyThreeArgumentMode() {
        val source =
            Files.createTempDirectory(
                "occasion-cli-source-"
            )

        try {
            val parsed =
                parseOccasionPackageBuildArguments(
                    arrayOf(
                        "1",
                        source.toString(),
                        source
                            .resolve("output")
                            .toString()
                    )
                )

            assertEquals(
                1L,
                parsed.occasionId
            )

            assertFalse(
                parsed.isMediaAware
            )

            assertEquals(
                null,
                parsed.mediaSourceDirectory
            )

            assertEquals(
                null,
                parsed.mediaLibraryRoot
            )
        } finally {
            source
                .toFile()
                .deleteRecursively()
        }
    }

    @Test
    fun parsesFiveArgumentMediaAwareMode() {
        val source =
            Files.createTempDirectory(
                "occasion-cli-source-"
            )

        val mediaExport =
            Files.createTempDirectory(
                "occasion-cli-media-export-"
            )

        val mediaLibrary =
            Files.createTempDirectory(
                "occasion-cli-media-library-"
            )

        try {
            val parsed =
                parseOccasionPackageBuildArguments(
                    arrayOf(
                        "1",
                        source.toString(),
                        mediaExport.toString(),
                        mediaLibrary.toString(),
                        source
                            .resolve("output")
                            .toString()
                    )
                )

            assertTrue(
                parsed.isMediaAware
            )

            assertEquals(
                mediaExport
                    .toAbsolutePath()
                    .normalize(),
                parsed.mediaSourceDirectory
            )

            assertEquals(
                mediaLibrary
                    .toAbsolutePath()
                    .normalize(),
                parsed.mediaLibraryRoot
            )
        } finally {
            source
                .toFile()
                .deleteRecursively()

            mediaExport
                .toFile()
                .deleteRecursively()

            mediaLibrary
                .toFile()
                .deleteRecursively()
        }
    }

    @Test
    fun rejectsUnsupportedArgumentCount() {
        assertFailsWith<
                IllegalArgumentException
                > {
            parseOccasionPackageBuildArguments(
                arrayOf(
                    "1",
                    "only-two-arguments"
                )
            )
        }
    }

    @Test
    fun rejectsMissingMediaLibrary() {
        val source =
            Files.createTempDirectory(
                "occasion-cli-source-"
            )

        val mediaExport =
            Files.createTempDirectory(
                "occasion-cli-media-export-"
            )

        try {
            assertFailsWith<
                    IllegalArgumentException
                    > {
                parseOccasionPackageBuildArguments(
                    arrayOf(
                        "1",
                        source.toString(),
                        mediaExport.toString(),
                        source
                            .resolve(
                                "missing-media-library"
                            )
                            .toString(),
                        source
                            .resolve("output")
                            .toString()
                    )
                )
            }
        } finally {
            source
                .toFile()
                .deleteRecursively()

            mediaExport
                .toFile()
                .deleteRecursively()
        }
    }
}