package org.syriacplatform.buildtools.source

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MediaSourceDataLoaderTest {

    private val loader =
        MediaSourceDataLoader()

    @Test
    fun loadsMediaExport() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/melodies/media-000001.mp3"
                "2","VIDEO","video/melodies/media-000002.mp4"
            """.trimIndent(),
            melodyMediaCsv = """
                "MelodyMediaID","MelodyN","MediaAssetID","Role","Sort","publicationStatus"
                "1","31","1","RECORDING","1","PUBLISHED"
                "2","602","2","RECORDING","1","PUBLISHED"
                "3","700","1","RECORDING","2","PUBLISHED"
            """.trimIndent()
        ) { directory ->
            val source =
                loader.load(directory)

            assertEquals(
                2,
                source.mediaAssets.size
            )
            assertEquals(
                3,
                source.melodyMedia.size
            )

            assertEquals(
                listOf(
                    1L,
                    2L
                ),
                source.mediaAssets
                    .map { it.id }
            )

            assertEquals(
                listOf(
                    31L,
                    602L,
                    700L
                ),
                source.melodyMedia
                    .map { it.melodyId }
            )
        }
    }

    @Test
    fun rejectsMissingMediaAssetReference() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/melodies/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = """
                "MelodyMediaID","MelodyN","MediaAssetID","Role","Sort","publicationStatus"
                "1","31","99","RECORDING","1","PUBLISHED"
            """.trimIndent()
        ) { directory ->
            val error =
                assertFailsWith<IllegalArgumentException> {
                    loader.load(directory)
                }

            assertTrue(
                error.message
                    .orEmpty()
                    .contains(
                        "missing MediaAssetID"
                    )
            )
        }
    }

    @Test
    fun rejectsDuplicateSourceRelativePath() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/melodies/media-000001.mp3"
                "2","AUDIO","AUDIO/MELODIES/MEDIA-000001.MP3"
            """.trimIndent(),
            melodyMediaCsv = """
                "MelodyMediaID","MelodyN","MediaAssetID","Role","Sort","publicationStatus"
                "1","31","1","RECORDING","1","PUBLISHED"
                "2","32","2","RECORDING","1","PUBLISHED"
            """.trimIndent()
        ) { directory ->
            val error =
                assertFailsWith<IllegalArgumentException> {
                    loader.load(directory)
                }

            assertTrue(
                error.message
                    .orEmpty()
                    .contains(
                        "duplicate SourceRelativePath"
                    )
            )
        }
    }

    @Test
    fun excludesArchiveMelodyMedia() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/melodies/media-000001.mp3"
                "2","AUDIO","audio/melodies/media-000002.mp3"
            """.trimIndent(),
            melodyMediaCsv = """
                "MelodyMediaID","MelodyN","MediaAssetID","Role","Sort","publicationStatus"
                "1","31","1","RECORDING","1","PUBLISHED"
                "2","31","2","RECORDING","2","ARCHIVE"
            """.trimIndent()
        ) { directory ->
            val source =
                loader.load(directory)

            assertEquals(
                listOf(1L),
                source.melodyMedia
                    .map { it.mediaAssetId }
            )
        }
    }

    @Test
    fun rejectsUnsupportedPublicationStatus() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/melodies/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = """
                "MelodyMediaID","MelodyN","MediaAssetID","Role","Sort","publicationStatus"
                "1","31","1","RECORDING","1","PRIVATE"
            """.trimIndent()
        ) { directory ->
            val error =
                assertFailsWith<IllegalArgumentException> {
                    loader.load(directory)
                }

            assertTrue(
                error.message
                    .orEmpty()
                    .contains(
                        "unsupported PublicationStatus"
                    )
            )
        }
    }

    @Test
    fun rejectsMissingPublicationStatus() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/melodies/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = """
                "MelodyMediaID","MelodyN","MediaAssetID","Role","Sort","publicationStatus"
                "1","31","1","RECORDING","1",
            """.trimIndent()
        ) { directory ->
            val error =
                assertFailsWith<IllegalArgumentException> {
                    loader.load(directory)
                }

            assertTrue(
                error.message
                    .orEmpty()
                    .contains(
                        "must have PublicationStatus"
                    )
            )
        }
    }

    private fun withMediaExport(
        mediaAssetCsv: String,
        melodyMediaCsv: String,
        block: (Path) -> Unit
    ) {
        val directory =
            Files.createTempDirectory(
                "syriacplatform-media-source-"
            )

        try {
            directory
                .resolve("MediaAsset.csv")
                .writeText(
                    mediaAssetCsv + "\n"
                )

            directory
                .resolve("MelodyMedia.csv")
                .writeText(
                    melodyMediaCsv + "\n"
                )

            block(directory)
        } finally {
            directory
                .toFile()
                .deleteRecursively()
        }
    }
}