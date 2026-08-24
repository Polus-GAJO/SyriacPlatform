package org.syriacplatform.buildtools.schema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.MediaSourceData
import org.syriacplatform.buildtools.source.models.MediaAssetSource
import org.syriacplatform.buildtools.source.models.MelodyMediaSource

class SchemaV1MediaMapperTest {

    private val mapper =
        SchemaV1MediaMapper()

    @Test
    fun mapsCanonicalMedia() {
        val canonical =
            mapper.map(
                MediaSourceData(
                    mediaAssets = listOf(
                        MediaAssetSource(
                            id = 7L,
                            mediaType = "AUDIO",
                            sourceRelativePath =
                                "audio/melodies/media-000007.mp3"
                        ),
                        MediaAssetSource(
                            id = 8L,
                            mediaType = "VIDEO",
                            sourceRelativePath =
                                "video/melodies/media-000008.mp4"
                        )
                    ),
                    melodyMedia = listOf(
                        MelodyMediaSource(
                            id = 1L,
                            melodyId = 602L,
                            mediaAssetId = 8L,
                            role = "RECORDING",
                            sort = 1L
                        ),
                        MelodyMediaSource(
                            id = 2L,
                            melodyId = 602L,
                            mediaAssetId = 7L,
                            role = "RECORDING",
                            sort = 2L
                        )
                    )
                )
            )

        assertEquals(
            2,
            canonical.mediaAssets.size
        )

        assertEquals(
            2,
            canonical.melodyMedia.size
        )

        assertTrue(
            canonical.hasRecording(
                602L
            )
        )

        assertFalse(
            canonical.hasRecording(
                999L
            )
        )
    }

    @Test
    fun recordingsForMelodyPreserveSortOrder() {
        val canonical =
            mapper.map(
                MediaSourceData(
                    mediaAssets = listOf(
                        MediaAssetSource(
                            id = 1L,
                            mediaType = "AUDIO",
                            sourceRelativePath =
                                "audio/melodies/media-000001.mp3"
                        ),
                        MediaAssetSource(
                            id = 2L,
                            mediaType = "VIDEO",
                            sourceRelativePath =
                                "video/melodies/media-000002.mp4"
                        )
                    ),
                    melodyMedia = listOf(
                        MelodyMediaSource(
                            id = 20L,
                            melodyId = 602L,
                            mediaAssetId = 1L,
                            role = "RECORDING",
                            sort = 2L
                        ),
                        MelodyMediaSource(
                            id = 10L,
                            melodyId = 602L,
                            mediaAssetId = 2L,
                            role = "RECORDING",
                            sort = 1L
                        )
                    )
                )
            )

        assertEquals(
            listOf(
                2L,
                1L
            ),
            canonical
                .recordingsForMelody(
                    602L
                )
                .map {
                    it.mediaAssetId
                }
        )
    }

    @Test
    fun sameMediaAssetCanServeMultipleMelodies() {
        val canonical =
            mapper.map(
                MediaSourceData(
                    mediaAssets = listOf(
                        MediaAssetSource(
                            id = 217L,
                            mediaType = "AUDIO",
                            sourceRelativePath =
                                "audio/melodies/media-000217.mp3"
                        )
                    ),
                    melodyMedia = listOf(
                        MelodyMediaSource(
                            id = 217L,
                            melodyId = 424L,
                            mediaAssetId = 217L,
                            role = "RECORDING",
                            sort = 1L
                        ),
                        MelodyMediaSource(
                            id = 473L,
                            melodyId = 2030L,
                            mediaAssetId = 217L,
                            role = "RECORDING",
                            sort = 1L
                        )
                    )
                )
            )

        assertEquals(
            217L,
            canonical
                .recordingsForMelody(
                    424L
                )
                .single()
                .mediaAssetId
        )

        assertEquals(
            217L,
            canonical
                .recordingsForMelody(
                    2030L
                )
                .single()
                .mediaAssetId
        )
    }
}