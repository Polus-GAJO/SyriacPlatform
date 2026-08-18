package org.syriacplatform.buildtools.schema

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader

class SchemaV1NavigationMapperTest {

    private val loader =
        AuthorSourceDataLoader()

    private val compositionMapper =
        SchemaV1CompositionMapper()

    private val navigationMapper =
        SchemaV1NavigationMapper()

    @Test
    fun representativeOccasionProducesCompleteNavigationContent() {
        val source =
            loadSource()

        val composition =
            compositionMapper.map(source)

        assertFalse(
            composition.hasBlockingDiagnostics
        )

        assertEquals(
            52,
            composition.resolvedItemCount
        )

        val navigation =
            navigationMapper.map(
                source = source,
                composition = composition
            )

        assertEquals(
            6,
            navigation.prayerSequences.size
        )

        assertEquals(
            52,
            navigation.prayerSequences.sumOf {
                it.liturgicalItemIds.size
            }
        )

        assertEquals(
            composition.prayers.map {
                it.orderedSourceItemIds
            },
            navigation.prayerSequences.map {
                it.liturgicalItemIds
            }
        )
    }

    @Test
    fun completeCompositionProducesDeterministicNavigation() {
        val source = loadSource()

        val completeComposition =
            SchemaV1CompositionDraft(
                occasionId = source.occasion.id,

                prayers =
                    source.prayers
                        .sortedBy { it.id }
                        .map { prayer ->
                            SchemaV1PrayerCompositionDraft(
                                prayerId = prayer.id,
                                orderedSourceItemIds =
                                    emptyList(),
                                resolvedItems =
                                    emptyList(),
                                blockedItemIds =
                                    emptyList()
                            )
                        },

                diagnostics = emptyList()
            )

        val navigation =
            navigationMapper.map(
                source = source,
                composition = completeComposition
            )

        assertEquals(
            1,
            navigation.entryPoints.size
        )

        assertEquals(
            1L,
            navigation.entryPoints.single().id
        )

        assertEquals(
            1L,
            navigation.entryPoints.single().occasionId
        )

        assertTrue(
            navigation.entryPoints.single().isDefault
        )

        assertEquals(
            1,
            navigation.occasions.size
        )

        assertEquals(
            1L,
            navigation.occasions.single().id
        )

        assertEquals(
            6,
            navigation.prayerSequences.size
        )

        assertEquals(
            listOf(
                1L,
                3L,
                4L,
                5L,
                9L,
                10L
            ),
            navigation.prayerSequences.map {
                it.prayerId
            }
        )

        assertEquals(
            listOf(
                4294967297L,
                4294967299L,
                4294967300L,
                4294967301L,
                4294967305L,
                4294967306L
            ),
            navigation.prayerSequences.map {
                it.id
            }
        )

        assertEquals(
            navigation.prayerSequences.map {
                it.id
            },
            navigation.occasions
                .single()
                .prayerSequenceIds
        )
    }

    private fun loadSource() =
        loader.load(
            representativeExportDirectory()
        )

    private fun representativeExportDirectory(): Path {
        return Path.of(
            "..",
            "author-database",
            "samples",
            "mapping-analysis"
        ).toAbsolutePath().normalize()
    }
}