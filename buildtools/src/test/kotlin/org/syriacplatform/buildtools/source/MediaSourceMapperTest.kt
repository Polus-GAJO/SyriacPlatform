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
    fun mapsExistsInMedia() {
        val relation =
            mapper.toExistsInMedia(
                CsvRow(
                    values = mapOf(
                        "ExistsInMediaID" to "41",
                        "ExistsInID" to "73",
                        "MediaAssetID" to "17",
                        "Role" to "PERFORMANCE",
                        "Sort" to "2",
                        "MediaTimingSetID" to "29"
                    )
                )
            )

        assertEquals(
            41L,
            relation.id
        )
        assertEquals(
            73L,
            relation.existsInId
        )
        assertEquals(
            17L,
            relation.mediaAssetId
        )
        assertEquals(
            "PERFORMANCE",
            relation.role
        )
        assertEquals(
            2L,
            relation.sort
        )
        assertEquals(
            29L,
            relation.mediaTimingSetId
        )
    }

    @Test
    fun mapsExistsInMediaWithoutTimingSet() {
        val relation =
            mapper.toExistsInMedia(
                CsvRow(
                    values = mapOf(
                        "ExistsInMediaID" to "42",
                        "ExistsInID" to "74",
                        "MediaAssetID" to "18",
                        "Role" to "PERFORMANCE",
                        "Sort" to "1",
                        "MediaTimingSetID" to ""
                    )
                )
            )

        assertEquals(
            null,
            relation.mediaTimingSetId
        )
    }

    @Test
    fun rejectsNonPerformanceExistsInMediaRole() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                mapper.toExistsInMedia(
                    CsvRow(
                        values = mapOf(
                            "ExistsInMediaID" to "1",
                            "ExistsInID" to "73",
                            "MediaAssetID" to "17",
                            "Role" to "RECORDING",
                            "Sort" to "1",
                            "MediaTimingSetID" to ""
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
    fun rejectsNonPositiveExistsInMediaSort() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                mapper.toExistsInMedia(
                    CsvRow(
                        values = mapOf(
                            "ExistsInMediaID" to "1",
                            "ExistsInID" to "73",
                            "MediaAssetID" to "17",
                            "Role" to "PERFORMANCE",
                            "Sort" to "0",
                            "MediaTimingSetID" to ""
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

    @Test
    fun mapsExistsInTextMediaSegment() {
        val relation =
            mapper.toExistsInTextMediaSegment(
                CsvRow(
                    values = mapOf(
                        "ExistsInTextMediaSegmentID" to "61",
                        "ExistsInTextID" to "104",
                        "MediaSegmentID" to "51"
                    )
                )
            )

        assertEquals(61L, relation.id)
        assertEquals(104L, relation.existsInTextId)
        assertEquals(51L, relation.mediaSegmentId)
    }

    @Test
    fun mapsMediaSegment() {
        val segment =
            mapper.toMediaSegment(
                CsvRow(
                    values = mapOf(
                        "MediaSegmentID" to "51",
                        "MediaTimingSetID" to "29",
                        "Sequence" to "3",
                        "StartMs" to "1250",
                        "EndMs" to "2840"
                    )
                )
            )

        assertEquals(51L, segment.id)
        assertEquals(29L, segment.mediaTimingSetId)
        assertEquals(3L, segment.sequence)
        assertEquals(1250L, segment.startMs)
        assertEquals(2840L, segment.endMs)
    }

    @Test
    fun convertsAuthorTimingAcrossMinuteBoundary() {
        val segment =
            mapper.toMediaSegment(
                CsvRow(
                    values = mapOf(
                        "MediaSegmentID" to "55",
                        "MediaTimingSetID" to "29",
                        "Sequence" to "5",
                        "StartMs" to "123456",
                        "EndMs" to "201789"
                    )
                )
            )

        assertEquals(
            83_456L,
            segment.startMs
        )
        assertEquals(
            121_789L,
            segment.endMs
        )
    }

    @Test
    fun mapsMediaSegmentWithoutTimingValues() {
        val segment =
            mapper.toMediaSegment(
                CsvRow(
                    values = mapOf(
                        "MediaSegmentID" to "52",
                        "MediaTimingSetID" to "29",
                        "Sequence" to "4",
                        "StartMs" to "",
                        "EndMs" to ""
                    )
                )
            )

        assertEquals(null, segment.startMs)
        assertEquals(null, segment.endMs)
    }

    @Test
    fun rejectsNonPositiveMediaSegmentSequence() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                mapper.toMediaSegment(
                    CsvRow(
                        values = mapOf(
                            "MediaSegmentID" to "53",
                            "MediaTimingSetID" to "29",
                            "Sequence" to "0",
                            "StartMs" to "",
                            "EndMs" to ""
                        )
                    )
                )
            }

        assertTrue(
            error.message
                .orEmpty()
                .contains("Sequence > 0")
        )
    }

    @Test
    fun rejectsNegativeMediaSegmentStartMs() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                mapper.toMediaSegment(
                    CsvRow(
                        values = mapOf(
                            "MediaSegmentID" to "54",
                            "MediaTimingSetID" to "29",
                            "Sequence" to "1",
                            "StartMs" to "-1",
                            "EndMs" to ""
                        )
                    )
                )
            }

        assertTrue(
            error.message
                .orEmpty()
                .contains("StartMs >= 0")
        )
    }

    @Test
    fun mapsMediaTimingSet() {
        val timingSet =
            mapper.toMediaTimingSet(
                CsvRow(
                    values = mapOf(
                        "MediaTimingSetID" to "29",
                        "MediaAssetID" to "17",
                        "Name" to "Main timing"
                    )
                )
            )

        assertEquals(
            29L,
            timingSet.id
        )
        assertEquals(
            17L,
            timingSet.mediaAssetId
        )
        assertEquals(
            "Main timing",
            timingSet.name
        )
    }

    @Test
    fun mapsMediaTimingSetWithoutName() {
        val timingSet =
            mapper.toMediaTimingSet(
                CsvRow(
                    values = mapOf(
                        "MediaTimingSetID" to "30",
                        "MediaAssetID" to "18",
                        "Name" to ""
                    )
                )
            )

        assertEquals(
            null,
            timingSet.name
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