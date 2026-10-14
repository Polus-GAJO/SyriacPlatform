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

    @Test
    fun prayerNotesFollowExistsInSortThenIdRegardlessOfInputOrder() {
        val base = loadSource()
        val prayerId = base.prayers.first().id
        fun noteRow(id: Long, sort: Long?, noteId: Long) =
            org.syriacplatform.buildtools.source.models.ExistsInSource(
                id = id, sort = sort, bookId = null, prayerId = prayerId,
                locationId = null, qoloId = null, qintoId = null,
                noteId = noteId, dayId = null, itemType = "PRAYER_NOTE"
            )
        val rows = listOf(
            noteRow(910L, null, 201L),
            noteRow(904L, 20L, 202L),
            noteRow(908L, 10L, 203L),
            noteRow(903L, 20L, 204L),
            noteRow(911L, 30L, 203L),
            noteRow(912L, 5L, 205L)
        )
        val source = base.copy(
            existsIn = base.existsIn + rows,
            occasionLinks = base.occasionLinks + rows.map {
                org.syriacplatform.buildtools.source.models.OccaExisSource(
                    id = it.id + 1000L,
                    existsInId = it.id,
                    occasionId = base.occasion.id
                )
            },
            notes = base.notes + listOf(
                org.syriacplatform.buildtools.source.models.NoteSource(201L, "Null sort", "PUBLISHED"),
                org.syriacplatform.buildtools.source.models.NoteSource(202L, "Sort 20, later ID", "PUBLISHED"),
                org.syriacplatform.buildtools.source.models.NoteSource(203L, "Sort 10", "PUBLISHED"),
                org.syriacplatform.buildtools.source.models.NoteSource(204L, "Sort 20, earlier ID", "PUBLISHED"),
                org.syriacplatform.buildtools.source.models.NoteSource(205L, "Archived earliest", "ARCHIVE")
            )
        )
        val composition = SchemaV1CompositionDraft(
            occasionId = source.occasion.id,
            prayers = listOf(
                SchemaV1PrayerCompositionDraft(
                    prayerId = prayerId,
                    orderedSourceItemIds = emptyList(),
                    resolvedItems = emptyList(),
                    blockedItemIds = emptyList()
                )
            ),
            diagnostics = emptyList()
        )

        val navigation = navigationMapper.map(source, composition)

        assertEquals(
            listOf(203L, 204L, 202L, 201L),
            navigation.prayerSequences.single().noteIds
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