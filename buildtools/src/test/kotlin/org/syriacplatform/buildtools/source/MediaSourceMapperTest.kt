package org.syriacplatform.buildtools.source

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MediaSourceMapperTest {

    private val mapper =
        MediaSourceMapper()

    @Test
    fun mapsMediaAsset() {
        val asset =
            mapper.toMediaAsset(
                CsvRow(
                    values = mapOf(
                        "MediaAssetID" to "17",
                        "MediaType" to "AUDIO",
                        "SourceRelativePath" to
                                "audio/melodies/media-000017.mp3"
                    )
                )
            )

        assertEquals(
            17L,
            asset.id
        )
        assertEquals(
            "AUDIO",
            asset.mediaType
        )
        assertEquals(
            "audio/melodies/media-000017.mp3",
            asset.sourceRelativePath
        )
    }

    @Test
    fun mapsMelodyMedia() {
        val relation =
            mapper.toMelodyMedia(
                CsvRow(
                    values = mapOf(
                        "MelodyMediaID" to "21",
                        "MelodyN" to "602",
                        "MediaAssetID" to "17",
                        "Role" to "RECORDING",
                        "Sort" to "2"
                    )
                )
            )

        assertEquals(
            21L,
            relation.id
        )
        assertEquals(
            602L,
            relation.melodyId
        )
        assertEquals(
            17L,
            relation.mediaAssetId
        )
        assertEquals(
            "RECORDING",
            relation.role
        )
        assertEquals(
            2L,
            relation.sort
        )
    }

    @Test
    fun rejectsUnsupportedMediaType() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                mapper.toMediaAsset(
                    CsvRow(
                        values = mapOf(
                            "MediaAssetID" to "1",
                            "MediaType" to "SOUND",
                            "SourceRelativePath" to
                                    "audio/a.mp3"
                        )
                    )
                )
            }

        assertTrue(
            error.message
                .orEmpty()
                .contains(
                    "unsupported MediaType"
                )
        )
    }

    @Test
    fun rejectsAbsoluteSourcePath() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                mapper.toMediaAsset(
                    CsvRow(
                        values = mapOf(
                            "MediaAssetID" to "1",
                            "MediaType" to "AUDIO",
                            "SourceRelativePath" to
                                    "D:\\SyriacPlatformMedia\\audio\\a.mp3"
                        )
                    )
                )
            }

        assertTrue(
            error.message
                .orEmpty()
                .contains(
                    "invalid SourceRelativePath"
                )
        )
    }

    @Test
    fun rejectsParentTraversal() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                mapper.toMediaAsset(
                    CsvRow(
                        values = mapOf(
                            "MediaAssetID" to "1",
                            "MediaType" to "AUDIO",
                            "SourceRelativePath" to
                                    "audio/../outside.mp3"
                        )
                    )
                )
            }

        assertTrue(
            error.message
                .orEmpty()
                .contains(
                    "invalid SourceRelativePath"
                )
        )
    }

    @Test
    fun rejectsNonRecordingMelodyMediaRole() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                mapper.toMelodyMedia(
                    CsvRow(
                        values = mapOf(
                            "MelodyMediaID" to "1",
                            "MelodyN" to "31",
                            "MediaAssetID" to "1",
                            "Role" to "PERFORMANCE",
                            "Sort" to "1"
                        )
                    )
                )
            }

        assertTrue(
            error.message
                .orEmpty()
                .contains(
                    "unsupported Role"
                )
        )
    }

    @Test
    fun rejectsNonPositiveSort() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                mapper.toMelodyMedia(
                    CsvRow(
                        values = mapOf(
                            "MelodyMediaID" to "1",
                            "MelodyN" to "31",
                            "MediaAssetID" to "1",
                            "Role" to "RECORDING",
                            "Sort" to "0"
                        )
                    )
                )
            }

        assertTrue(
            error.message
                .orEmpty()
                .contains(
                    "Sort > 0"
                )
        )
    }
}