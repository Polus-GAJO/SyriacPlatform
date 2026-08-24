package org.syriacplatform.buildtools.schema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.AuthorSourceData
import org.syriacplatform.buildtools.source.models.MelodySource

class SchemaV1CanonicalMediaIntegrationTest {

    private val mapper =
        SchemaV1CanonicalMapper()

    @Test
    fun mediaAwareMappingUsesMelodyMediaAsRecordingTruth() {
        val source =
            minimalSource(
                melodies = listOf(
                    melody(
                        id = 31L,
                        legacyHasRecording = false
                    ),
                    melody(
                        id = 32L,
                        legacyHasRecording = true
                    )
                )
            )

        val media =
            SchemaV1CanonicalMedia(
                mediaAssets = listOf(
                    SchemaV1MediaAsset(
                        id = 7L,
                        mediaType = "AUDIO",
                        sourceRelativePath =
                            "audio/melodies/media-000007.mp3"
                    )
                ),
                melodyMedia = listOf(
                    SchemaV1MelodyMedia(
                        id = 1L,
                        melodyId = 31L,
                        mediaAssetId = 7L,
                        role = "RECORDING",
                        sort = 1L
                    )
                )
            )

        val canonical =
            mapper.map(
                source = source,
                media = media
            )

        val melody31 =
            canonical.melodies.single {
                it.id == 31L
            }

        val melody32 =
            canonical.melodies.single {
                it.id == 32L
            }

        assertTrue(
            melody31.hasRecording
        )

        assertEquals(
            listOf(7L),
            melody31.recordingIds
        )

        assertFalse(
            melody32.hasRecording
        )

        assertTrue(
            melody32.recordingIds.isEmpty()
        )
    }

    @Test
    fun mediaAwareMappingPreservesRecordingSortOrder() {
        val source =
            minimalSource(
                melodies = listOf(
                    melody(
                        id = 602L,
                        legacyHasRecording = false
                    )
                )
            )

        val media =
            SchemaV1CanonicalMedia(
                mediaAssets = listOf(
                    SchemaV1MediaAsset(
                        id = 292L,
                        mediaType = "VIDEO",
                        sourceRelativePath =
                            "video/melodies/media-000292.mp4"
                    ),
                    SchemaV1MediaAsset(
                        id = 293L,
                        mediaType = "AUDIO",
                        sourceRelativePath =
                            "audio/melodies/media-000293.mp3"
                    )
                ),
                melodyMedia = listOf(
                    SchemaV1MelodyMedia(
                        id = 293L,
                        melodyId = 602L,
                        mediaAssetId = 293L,
                        role = "RECORDING",
                        sort = 2L
                    ),
                    SchemaV1MelodyMedia(
                        id = 292L,
                        melodyId = 602L,
                        mediaAssetId = 292L,
                        role = "RECORDING",
                        sort = 1L
                    )
                )
            )

        val mapped =
            mapper.map(
                source = source,
                media = media
            ).melodies.single()

        assertEquals(
            listOf(
                292L,
                293L
            ),
            mapped.recordingIds
        )
    }

    private fun melody(
        id: Long,
        legacyHasRecording: Boolean
    ): MelodySource {
        return MelodySource(
            id = id,
            qoloId = 1L,
            name = "Melody $id",
            searchName = "Melody $id",
            qintoId = null,
            occasionId = null,
            noteId = null,
            hasRecording =
                legacyHasRecording
        )
    }

    private fun minimalSource(
        melodies: List<MelodySource>
    ): AuthorSourceData {
        return AuthorSourceData(
            occasion =
                org.syriacplatform.buildtools.source.models
                    .OccasionSource(
                        sort = 1L,
                        id = 1L,
                        name = "Occasion",
                        day = null,
                        monthId = null
                    ),
            prayers = emptyList(),
            occasionLinks = emptyList(),
            existsIn = emptyList(),
            existsInTexts = emptyList(),
            petExis = emptyList(),
            qolos = emptyList(),
            texts = emptyList(),
            petgomos = emptyList(),
            melodies = melodies,
            qintos = emptyList()
        )
    }
}