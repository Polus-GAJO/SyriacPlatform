package org.syriacplatform.buildtools.schema

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader
import kotlin.test.assertFalse

class SchemaV1CompositionMapperTest {

    private val loader =
        AuthorSourceDataLoader()

    private val mapper =
        SchemaV1CompositionMapper()

    @Test
    fun buildsRepresentativeOccasionCompositionDraft() {
        val draft = mapRepresentativeSource()

        assertEquals(
            1L,
            draft.occasionId
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
            draft.prayers.map {
                it.prayerId
            }
        )

        assertEquals(
            52,
            draft.prayers.sumOf {
                it.orderedSourceItemIds.size
            }
        )

        assertEquals(
            52,
            draft.resolvedItemCount
        )

        assertEquals(
            0,
            draft.blockedItemCount
        )

        assertFalse(
            draft.hasBlockingDiagnostics
        )
    }

    @Test
    fun preservesEverySourceOccurrenceInCompositionAccounting() {
        val source = loadSource()
        val draft = mapper.map(source)

        val selectedIds =
            source.occasionLinks
                .mapNotNull {
                    it.existsInId
                }
                .toSet()

        val accountedIds =
            draft.prayers
                .flatMap {
                    it.orderedSourceItemIds
                }
                .toSet()

        assertEquals(
            selectedIds,
            accountedIds
        )
    }

    @Test
    fun resolvedItemsPreserveExistsInIdentity() {
        val draft = mapRepresentativeSource()

        val resolvedIds =
            draft.prayers
                .flatMap {
                    it.resolvedItems
                }
                .map {
                    it.id
                }

        val sourceIds =
            draft.prayers
                .flatMap {
                    it.orderedSourceItemIds
                }

        assertTrue(
            resolvedIds.all {
                it in sourceIds
            }
        )

        assertEquals(
            resolvedIds.size,
            resolvedIds.distinct().size
        )
    }

    @Test
    fun preservesAllMelodyResolutionStatesWithoutBlockingQoloOccurrence() {
        val draft =
            mapRepresentativeSource()

        val items =
            draft.prayers
                .flatMap {
                    it.resolvedItems
                }
                .filterIsInstance<
                        SchemaV1QoloLiturgicalItem
                        >()

        assertEquals(
            52,
            items.size
        )

        assertEquals(
            20,
            items.count {
                it.effectiveMelodyId != null
            }
        )

        assertEquals(
            32,
            items.count {
                it.effectiveMelodyId == null
            }
        )

        assertEquals(
            3,
            items.count {
                it.melodyCandidateIds.size > 1
            }
        )

        assertEquals(
            29,
            items.count {
                it.effectiveMelodyId == null &&
                        it.melodyCandidateIds.isEmpty()
            }
        )

        assertTrue(
            draft.diagnostics.isEmpty()
        )
    }

    @Test
    fun resolvedQoloItemsContainContextualVerses() {
        val source = loadSource()
        val draft = mapper.map(source)

        val resolvedItems =
            draft.prayers.flatMap {
                it.resolvedItems
            }

        assertTrue(
            resolvedItems.isNotEmpty()
        )

        resolvedItems.forEach { item ->
            val expectedTextIds =
                source.existsInTexts
                    .filter {
                        it.existsInId == item.id
                    }
                    .sortedWith(
                        compareBy(
                            {
                                it.sortInPrayer
                                    ?: Int.MAX_VALUE
                            },
                            { it.id }
                        )
                    )
                    .mapNotNull {
                        it.textId
                    }

            assertEquals(
                expectedTextIds,
                item.verses.map {
                    it.textId
                }
            )
        }
    }

    @Test
    fun compositionIsDeterministic() {
        val source = loadSource()

        val first =
            mapper.map(source)

        val second =
            mapper.map(source)

        assertEquals(
            first,
            second
        )
    }

    private fun mapRepresentativeSource():
            SchemaV1CompositionDraft {

        return mapper.map(
            loadSource()
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