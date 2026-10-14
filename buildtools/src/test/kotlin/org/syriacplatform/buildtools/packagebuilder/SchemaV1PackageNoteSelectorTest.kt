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