package org.syriacplatform.buildtools.schema

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader
import kotlin.test.assertFalse
import org.syriacplatform.buildtools.source.models.ExistsInSource

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

        val occasionExistsInIds =
    source.occasionLinks
        .filter {
            it.occasionId == source.occasion.id
        }
        .mapNotNull {
            it.existsInId
        }
        .toSet()

val selectedIds =
    source.existsIn
        .filter {
            it.id in occasionExistsInIds &&
                it.itemType != "PRAYER_NOTE"
        }
        .map {
            it.id
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
    fun contextualVersesPreserveExistsInTextIdentity() {
        val source = loadSource()
        val draft = mapper.map(source)

        val resolvedItems =
            draft.prayers.flatMap {
                it.resolvedItems
            }

        resolvedItems.forEach { item ->
            val expectedOccurrenceIds =
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
                    .map {
                        it.id
                    }

            assertEquals(
                expectedOccurrenceIds,
                item.verses.map {
                    it.id
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

    @Test
fun excludesPrayerNoteFromLiturgicalComposition() {
    val originalSource = loadSource()

    val prayerId = originalSource.prayers.first().id
    val noteItemId = -1001L

    val prayerNote = ExistsInSource(
        id = noteItemId,
        sort = null,
        bookId = null,
        prayerId = prayerId,
        locationId = null,
        qoloId = null,
        qintoId = null,
        noteId = 1001L,
        dayId = null,
        itemType = "PRAYER_NOTE"
    )

    val source = originalSource.copy(
        existsIn = originalSource.existsIn + prayerNote,
        occasionLinks = originalSource.occasionLinks +
            originalSource.occasionLinks.first().copy(
                existsInId = noteItemId
            )
    )

    val originalDraft = mapper.map(originalSource)
    val draft = mapper.map(source)

    val orderedIds = draft.prayers.flatMap {
        it.orderedSourceItemIds
    }

    val resolvedIds = draft.prayers.flatMap {
        it.resolvedItems
    }.map {
        it.id
    }

    assertFalse(noteItemId in orderedIds)
    assertFalse(noteItemId in resolvedIds)

    assertEquals(
        originalDraft.prayers,
        draft.prayers
    )

    assertTrue(
        source.existsIn.any {
            it.id == noteItemId &&
                it.itemType == "PRAYER_NOTE" &&
                it.noteId == 1001L
        }
    )
}

    @Test
    fun sameCanonicalQoloOccurrencesKeepTheirOwnNotes() {
        val original = loadSource()
        val linkedIds = original.occasionLinks
            .filter { it.occasionId == original.occasion.id }
            .mapNotNull { it.existsInId }
            .toSet()
        val pair = original.existsIn
            .filter { it.id in linkedIds && it.itemType != "PRAYER_NOTE" && it.qoloId != null }
            .groupBy { it.qoloId }
            .values
            .firstOrNull { it.size >= 2 }
            ?.take(2)
            ?: error("Representative fixture needs two occurrences of one Qolo")
        val firstId = pair[0].id
        val secondId = pair[1].id
        val source = original.copy(
            existsIn = original.existsIn.map { item ->
                when (item.id) {
                    firstId -> item.copy(noteId = 99001L)
                    secondId -> item.copy(noteId = 99002L)
                    else -> item
                }
            },
            notes = original.notes + listOf(
                org.syriacplatform.buildtools.source.models.NoteSource(
                    99001L, "First occurrence note", "PUBLISHED"
                ),
                org.syriacplatform.buildtools.source.models.NoteSource(
                    99002L, "Second occurrence note", "PUBLISHED"
                )
            )
        )

        val draft = mapper.map(source)
        val items = draft.prayers.flatMap { it.resolvedItems }
        val first = items.single { it.id == firstId }
        val second = items.single { it.id == secondId }

        assertEquals(pair[0].qoloId, pair[1].qoloId)
        assertEquals(99001L, first.noteId)
        assertEquals(99002L, second.noteId)
        assertEquals(
            listOf(99001L, 99002L),
            listOf(first.noteId, second.noteId)
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