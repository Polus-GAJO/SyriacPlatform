package org.syriacplatform.buildtools.packagebuilder

import org.syriacplatform.buildtools.source.AuthorSourceData

class SchemaV1PackageNoteSelector {

    fun selectOccasionNotes(
        source: AuthorSourceData
    ): List<SchemaV1PackageNote> {

        val occasionNoteId =
            source.occasion.noteId
                ?: return emptyList()

        return source.notes
            .filter { note ->
                note.id == occasionNoteId &&
                    note.publicationStatus == "PUBLISHED"
            }
            .map { note ->
                SchemaV1PackageNote(
                    id = note.id,
                    text = note.text
                )
            }
    }
}