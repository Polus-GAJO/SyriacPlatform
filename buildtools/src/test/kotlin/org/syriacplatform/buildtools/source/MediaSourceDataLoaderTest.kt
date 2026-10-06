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
                1,
                source.existsInMedia.size
            )
            assertEquals(
                90L,
                source.existsInMedia.single().existsInId
            )
            assertEquals(
                1L,
                source.existsInMedia.single().mediaAssetId
            )
            assertEquals(
                15L,
                source.existsInMedia.single().mediaTimingSetId
            )
            assertEquals(
                1,
                source.mediaTimingSets.size
            )
            assertEquals(
                15L,
                source.mediaTimingSets.single().id
            )
            assertEquals(
                1L,
                source.mediaTimingSets.single().mediaAssetId
            )
            assertEquals(
                2,
                source.mediaSegments.size
            )
            assertEquals(
                listOf(1L, 2L),
                source.mediaSegments.map { it.sequence }
            )
            assertEquals(
                0L,
                source.mediaSegments.first().startMs
            )
            assertEquals(
                null,
                source.mediaSegments.last().endMs
            )
            assertEquals(
                2,
                source.existsInTextMediaSegments.size
            )
            assertEquals(
                listOf(101L, 102L),
                source.existsInTextMediaSegments.map { it.existsInTextId }
            )
            assertEquals(
                listOf(21L, 22L),
                source.existsInTextMediaSegments.map { it.mediaSegmentId }
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
    fun excludesArchiveExistsInMedia() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
                "2","AUDIO","audio/performances/media-000002.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            existsInMediaCsv = """
                "ExistsInMediaID","ExistsInID","MediaAssetID","Role","Sort","MediaTimingSetID","publicationStatus"
                "1","90","1","PERFORMANCE","1","15","PUBLISHED"
                "2","90","2","PERFORMANCE","2","16","ARCHIVE"
            """.trimIndent()
        ) { directory ->
            val source =
                loader.load(directory)

            assertEquals(
                listOf(1L),
                source.existsInMedia
                    .map { it.mediaAssetId }
            )
        }
    }

    @Test
    fun rejectsMissingExistsInMediaAssetReference() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            existsInMediaCsv = """
                "ExistsInMediaID","ExistsInID","MediaAssetID","Role","Sort","MediaTimingSetID","publicationStatus"
                "1","90","99","PERFORMANCE","1","15","PUBLISHED"
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
                        "ExistsInMedia.csv references missing MediaAssetID"
                    )
            )
        }
    }

    @Test
    fun rejectsDuplicateExistsInMediaId() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            existsInMediaCsv = """
                "ExistsInMediaID","ExistsInID","MediaAssetID","Role","Sort","MediaTimingSetID","publicationStatus"
                "1","90","1","PERFORMANCE","1","15","PUBLISHED"
                "1","91","1","PERFORMANCE","1","16","PUBLISHED"
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
                        "duplicate ExistsInMediaID"
                    )
            )
        }
    }

    @Test
    fun rejectsUnsupportedExistsInMediaPublicationStatus() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            existsInMediaCsv = """
                "ExistsInMediaID","ExistsInID","MediaAssetID","Role","Sort","MediaTimingSetID","publicationStatus"
                "1","90","1","PERFORMANCE","1","15","PRIVATE"
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
    fun rejectsMissingExistsInMediaPublicationStatus() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            existsInMediaCsv = """
                "ExistsInMediaID","ExistsInID","MediaAssetID","Role","Sort","MediaTimingSetID","publicationStatus"
                "1","90","1","PERFORMANCE","1","15",
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

    @Test
    fun rejectsDuplicateMediaTimingSetId() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            mediaTimingSetCsv = """
                "MediaTimingSetID","MediaAssetID","Name"
                "15","1","First"
                "15","1","Second"
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
                        "duplicate MediaTimingSetID"
                    )
            )
        }
    }

    @Test
    fun rejectsMissingMediaTimingSetAssetReference() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            mediaTimingSetCsv = """
                "MediaTimingSetID","MediaAssetID","Name"
                "15","99",""
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
                        "MediaTimingSet.csv references missing MediaAssetID"
                    )
            )
        }
    }

    @Test
    fun rejectsMissingExistsInMediaTimingSetReference() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            existsInMediaCsv = """
                "ExistsInMediaID","ExistsInID","MediaAssetID","Role","Sort","MediaTimingSetID","publicationStatus"
                "1","90","1","PERFORMANCE","1","99","PUBLISHED"
            """.trimIndent(),
            mediaTimingSetCsv = """
                "MediaTimingSetID","MediaAssetID","Name"
                "15","1",""
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
                        "references missing MediaTimingSetID"
                    )
            )
        }
    }

    @Test
    fun rejectsDuplicateMediaSegmentId() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            mediaSegmentCsv = """
                "MediaSegmentID","MediaTimingSetID","Sequence","StartMs","EndMs"
                "21","15","1","0","1000"
                "21","15","2","1000","2000"
            """.trimIndent()
        ) { directory ->
            val error =
                assertFailsWith<IllegalArgumentException> {
                    loader.load(directory)
                }

            assertTrue(
                error.message
                    .orEmpty()
                    .contains("duplicate MediaSegmentID")
            )
        }
    }

    @Test
    fun rejectsMissingMediaSegmentTimingSetReference() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            mediaSegmentCsv = """
                "MediaSegmentID","MediaTimingSetID","Sequence","StartMs","EndMs"
                "21","99","1","0","1000"
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
                        "MediaSegment.csv references missing MediaTimingSetID"
                    )
            )
        }
    }

    @Test
    fun rejectsDuplicateExistsInTextMediaSegmentId() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            existsInTextMediaSegmentCsv = """
                "ExistsInTextMediaSegmentID","ExistsInTextID","MediaSegmentID"
                "31","101","21"
                "31","102","22"
            """.trimIndent()
        ) { directory ->
            val error =
                assertFailsWith<IllegalArgumentException> {
                    loader.load(directory)
                }

            assertTrue(
                error.message
                    .orEmpty()
                    .contains("duplicate ExistsInTextMediaSegmentID")
            )
        }
    }

    @Test
    fun rejectsMissingTextLinkMediaSegmentReference() {
        withMediaExport(
            mediaAssetCsv = """
                "MediaAssetID","MediaType","SourceRelativePath"
                "1","AUDIO","audio/performances/media-000001.mp3"
            """.trimIndent(),
            melodyMediaCsv = EMPTY_MELODY_MEDIA_CSV,
            existsInTextMediaSegmentCsv = """
                "ExistsInTextMediaSegmentID","ExistsInTextID","MediaSegmentID"
                "31","999","99"
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
                        "ExistsInTextMediaSegment.csv references missing MediaSegmentID"
                    )
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
        existsInMediaCsv: String = DEFAULT_EXISTS_IN_MEDIA_CSV,
        mediaTimingSetCsv: String = DEFAULT_MEDIA_TIMING_SET_CSV,
        mediaSegmentCsv: String = DEFAULT_MEDIA_SEGMENT_CSV,
        existsInTextMediaSegmentCsv: String =
            DEFAULT_EXISTS_IN_TEXT_MEDIA_SEGMENT_CSV,
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

            directory
                .resolve("ExistsInMedia.csv")
                .writeText(
                    existsInMediaCsv + "\n"
                )

            directory
                .resolve("MediaTimingSet.csv")
                .writeText(
                    mediaTimingSetCsv + "\n"
                )

            directory
                .resolve("MediaSegment.csv")
                .writeText(
                    mediaSegmentCsv + "\n"
                )

            directory
                .resolve("ExistsInTextMediaSegment.csv")
                .writeText(
                    existsInTextMediaSegmentCsv + "\n"
                )

            block(directory)
        } finally {
            directory
                .toFile()
                .deleteRecursively()
        }
    }

    private companion object {
        val DEFAULT_EXISTS_IN_MEDIA_CSV =
            """
                "ExistsInMediaID","ExistsInID","MediaAssetID","Role","Sort","MediaTimingSetID","publicationStatus"
                "1","90","1","PERFORMANCE","1","15","PUBLISHED"
            """.trimIndent()

        val DEFAULT_MEDIA_TIMING_SET_CSV =
            """
                "MediaTimingSetID","MediaAssetID","Name"
                "15","1",""
            """.trimIndent()

        val DEFAULT_MEDIA_SEGMENT_CSV =
            """
                "MediaSegmentID","MediaTimingSetID","Sequence","StartMs","EndMs"
                "21","15","1","0","1000"
                "22","15","2","1000",""
            """.trimIndent()

        val DEFAULT_EXISTS_IN_TEXT_MEDIA_SEGMENT_CSV =
            """
                "ExistsInTextMediaSegmentID","ExistsInTextID","MediaSegmentID"
                "31","101","21"
                "32","102","22"
            """.trimIndent()

        val EMPTY_MELODY_MEDIA_CSV =
            """
                "MelodyMediaID","MelodyN","MediaAssetID","Role","Sort","publicationStatus"
            """.trimIndent()
    }
}