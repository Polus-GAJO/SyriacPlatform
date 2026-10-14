package org.syriacplatform.buildtools.packagebuilder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.AuthorSourceData
import org.syriacplatform.buildtools.source.models.NoteSource
import org.syriacplatform.buildtools.source.models.OccasionSource

class SchemaV1PackageNoteSelectorTest {

    private val selector = SchemaV1PackageNoteSelector()

    @Test
    fun selectsPublishedOccasionNote() {
        val source = source(
            noteId = 101L,
            notes = listOf(
                NoteSource(101L, "Occasion note", "PUBLISHED"),
                NoteSource(102L, "Other note", "PUBLISHED")
            )
        )

        val result = selector.selectOccasionNotes(source)

        assertEquals(
            listOf(SchemaV1PackageNote(101L, "Occasion note")),
            result
        )
    }

    @Test
    fun excludesArchivedOccasionNote() {
        val source = source(
            noteId = 101L,
            notes = listOf(
                NoteSource(101L, "Archived note", "ARCHIVE")
            )
        )

        val result = selector.selectOccasionNotes(source)

        assertTrue(result.isEmpty())
    }

    @Test
    fun returnsEmptyWhenOccasionHasNoNote() {
        val source = source(
            noteId = null,
            notes = listOf(
                NoteSource(101L, "Other note", "PUBLISHED")
            )
        )

        val result = selector.selectOccasionNotes(source)

        assertTrue(result.isEmpty())
    }

    @Test
    fun selectsOnlyPublishedNotesAcrossAllThreeScopes() {
        val source = source(
            noteId = 101L,
            notes = listOf(
                NoteSource(101L, "Published occasion", "PUBLISHED"),
                NoteSource(102L, "Archived prayer", "ARCHIVE"),
                NoteSource(103L, "Published prayer", "PUBLISHED"),
                NoteSource(104L, "Archived occurrence", "ARCHIVE"),
                NoteSource(105L, "Published occurrence", "PUBLISHED"),
                NoteSource(106L, "Unreferenced published", "PUBLISHED")
            )
        )
        val navigation = org.syriacplatform.buildtools.schema.SchemaV1NavigationContent(
            entryPoints = emptyList(),
            occasions = listOf(
                org.syriacplatform.buildtools.schema.SchemaV1Occasion(
                    id = 1L,
                    name = "Test occasion",
                    prayerSequenceIds = listOf(10L),
                    noteId = 101L
                )
            ),
            prayerSequences = listOf(
                org.syriacplatform.buildtools.schema.SchemaV1PrayerSequence(
                    id = 10L,
                    prayerId = 20L,
                    liturgicalItemIds = listOf(30L, 31L),
                    noteIds = listOf(102L, 103L)
                )
            )
        )
        val composition = org.syriacplatform.buildtools.schema.SchemaV1CompositionDraft(
            occasionId = 1L,
            prayers = listOf(
                org.syriacplatform.buildtools.schema.SchemaV1PrayerCompositionDraft(
                    prayerId = 20L,
                    orderedSourceItemIds = listOf(30L, 31L),
                    resolvedItems = listOf(
                        org.syriacplatform.buildtools.schema.SchemaV1UnresolvedQoloLiturgicalItem(
                            id = 30L,
                            verses = emptyList(),
                            noteId = 104L
                        ),
                        org.syriacplatform.buildtools.schema.SchemaV1UnresolvedQoloLiturgicalItem(
                            id = 31L,
                            verses = emptyList(),
                            noteId = 105L
                        )
                    ),
                    blockedItemIds = emptyList()
                )
            ),
            diagnostics = emptyList()
        )

        assertEquals(
            listOf(
                SchemaV1PackageNote(101L, "Published occasion"),
                SchemaV1PackageNote(103L, "Published prayer"),
                SchemaV1PackageNote(105L, "Published occurrence")
            ),
            selector.selectNotes(source, navigation, composition)
        )
    }

    private fun source(
        noteId: Long?,
        notes: List<NoteSource>
    ): AuthorSourceData =
        AuthorSourceData(
            occasion = OccasionSource(
                sort = 1L,
                id = 1L,
                name = "Test occasion",
                day = null,
                monthId = null,
                noteId = noteId
            ),
            prayers = emptyList(),
            occasionLinks = emptyList(),
            existsIn = emptyList(),
            existsInTexts = emptyList(),
            petExis = emptyList(),
            qolos = emptyList(),
            texts = emptyList(),
            petgomos = emptyList(),
            melodies = emptyList(),
            qintos = emptyList(),
            notes = notes
        )
}