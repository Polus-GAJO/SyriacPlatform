package org.syriacplatform.buildtools.schema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SchemaV1PackageMediaSelectorTest {

    private val selector =
        SchemaV1PackageMediaSelector()

    @Test
    fun selectsOnlyMediaForRequestedMelodies() {
        val result =
            selector.select(
                canonicalMedia = canonicalMedia(),
                melodyIds = setOf(
                    602L
                )
            )

        assertEquals(
            listOf(
                292L,
                293L
            ),
            result.mediaAssets.map {
                it.id
            }
        )

        assertEquals(
            listOf(
                602L,
                602L
            ),
            result.melodyMedia.map {
                it.melodyId
            }
        )

        assertEquals(
            listOf(
                1L,
                2L
            ),
            result.melodyMedia.map {
                it.sort
            }
        )
    }

    @Test
    fun preservesSharedMediaAssetOnce() {
        val result =
            selector.select(
                canonicalMedia = canonicalMedia(),
                melodyIds = setOf(
                    424L,
                    2030L
                )
            )

        assertEquals(
            listOf(
                217L
            ),
            result.mediaAssets.map {
                it.id
            }
        )

        assertEquals(
            2,
            result.melodyMedia.size
        )

        assertTrue(
            result.melodyMedia.all {
                it.mediaAssetId == 217L
            }
        )
    }

    @Test
    fun ignoresUnselectedGlobalMedia() {
        val result =
            selector.select(
                canonicalMedia = canonicalMedia(),
                melodyIds = setOf(
                    424L
                )
            )

        assertEquals(
            listOf(
                217L
            ),
            result.mediaAssets.map {
                it.id
            }
        )

        assertEquals(
            listOf(
                424L
            ),
            result.melodyMedia.map {
                it.melodyId
            }
        )
    }

    @Test
    fun emptyMelodySetProducesEmptyMediaSlice() {
        val result =
            selector.select(
                canonicalMedia = canonicalMedia(),
                melodyIds = emptySet()
            )

        assertTrue(
            result.mediaAssets.isEmpty()
        )

        assertTrue(
            result.melodyMedia.isEmpty()
        )
    }

    private fun canonicalMedia(): SchemaV1CanonicalMedia {
        return SchemaV1CanonicalMedia(
            mediaAssets = listOf(
                SchemaV1MediaAsset(
                    id = 217L,
                    mediaType = "AUDIO",
                    sourceRelativePath =
                        "audio/melodies/media-000217.mp3"
                ),
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
                ),
                SchemaV1MediaAsset(
                    id = 466L,
                    mediaType = "AUDIO",
                    sourceRelativePath =
                        "audio/melodies/media-000466.mp4"
                )
            ),
            melodyMedia = listOf(
                SchemaV1MelodyMedia(
                    id = 217L,
                    melodyId = 424L,
                    mediaAssetId = 217L,
                    role = "RECORDING",
                    sort = 1L
                ),
                SchemaV1MelodyMedia(
                    id = 292L,
                    melodyId = 602L,
                    mediaAssetId = 292L,
                    role = "RECORDING",
                    sort = 1L
                ),
                SchemaV1MelodyMedia(
                    id = 293L,
                    melodyId = 602L,
                    mediaAssetId = 293L,
                    role = "RECORDING",
                    sort = 2L
                ),
                SchemaV1MelodyMedia(
                    id = 469L,
                    melodyId = 1964L,
                    mediaAssetId = 466L,
                    role = "RECORDING",
                    sort = 1L
                ),
                SchemaV1MelodyMedia(
                    id = 473L,
                    melodyId = 2030L,
                    mediaAssetId = 217L,
                    role = "RECORDING",
                    sort = 1L
                )
            )
        )
    }
}