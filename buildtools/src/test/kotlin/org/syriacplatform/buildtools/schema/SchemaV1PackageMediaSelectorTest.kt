package org.syriacplatform.buildtools.schema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

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

    @Test
    fun selectsReachablePerformanceTimingDeterministically() {
        val media =
            SchemaV1CanonicalMedia(
                mediaAssets = listOf(
                    SchemaV1MediaAsset(301L, "AUDIO", "audio/performance-301.mp3"),
                    SchemaV1MediaAsset(302L, "AUDIO", "audio/performance-302.mp3")
                ),
                melodyMedia = emptyList(),
                performanceMedia = listOf(
                    SchemaV1PerformanceMedia(402L, 502L, 302L, "PERFORMANCE", 1L, 702L),
                    SchemaV1PerformanceMedia(401L, 501L, 301L, "PERFORMANCE", 1L, 701L)
                ),
                mediaTimingSets = listOf(
                    SchemaV1MediaTimingSet(702L, 302L, null),
                    SchemaV1MediaTimingSet(701L, 301L, null)
                ),
                mediaSegments = listOf(
                    SchemaV1MediaSegment(802L, 702L, 1L, 0L, 1000L),
                    SchemaV1MediaSegment(801L, 701L, 1L, 0L, 1000L)
                ),
                textOccurrenceMediaSegments = listOf(
                    SchemaV1TextOccurrenceMediaSegment(902L, 9002L, 802L),
                    SchemaV1TextOccurrenceMediaSegment(901L, 9001L, 801L)
                )
            )

        val result =
            selector.select(
                canonicalMedia = media,
                melodyIds = emptySet(),
                liturgicalItems = listOf(
                    SchemaV1UnresolvedQoloLiturgicalItem(
                        id = 501L,
                        verses = listOf(
                            SchemaV1TextOccurrence(9001L, 601L, null)
                        )
                    )
                )
            )

        assertEquals(listOf(301L), result.mediaAssets.map { it.id })
        assertEquals(listOf(401L), result.performanceMedia.map { it.id })
        assertEquals(listOf(701L), result.mediaTimingSets.map { it.id })
        assertEquals(listOf(801L), result.mediaSegments.map { it.id })
        assertEquals(
            listOf(901L),
            result.textOccurrenceMediaSegments.map { it.id }
        )
    }

    @Test
    fun rejectsSelectedTimingThatReferencesOccurrenceOutsidePackage() {
        val media =
            SchemaV1CanonicalMedia(
                mediaAssets = listOf(
                    SchemaV1MediaAsset(301L, "AUDIO", "audio/performance-301.mp3")
                ),
                melodyMedia = emptyList(),
                performanceMedia = listOf(
                    SchemaV1PerformanceMedia(401L, 501L, 301L, "PERFORMANCE", 1L, 701L)
                ),
                mediaTimingSets = listOf(
                    SchemaV1MediaTimingSet(701L, 301L, null)
                ),
                mediaSegments = listOf(
                    SchemaV1MediaSegment(801L, 701L, 1L, 0L, 1000L)
                ),
                textOccurrenceMediaSegments = listOf(
                    SchemaV1TextOccurrenceMediaSegment(901L, 9999L, 801L)
                )
            )

        assertFailsWith<IllegalArgumentException> {
            selector.select(
                canonicalMedia = media,
                melodyIds = emptySet(),
                liturgicalItems = listOf(
                    SchemaV1UnresolvedQoloLiturgicalItem(
                        id = 501L,
                        verses = listOf(
                            SchemaV1TextOccurrence(9001L, 601L, null)
                        )
                    )
                )
            )
        }
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