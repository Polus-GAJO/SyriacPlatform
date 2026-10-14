package org.syriacplatform.buildtools.packagebuilder

import org.syriacplatform.buildtools.source.AuthorSourceData
import org.syriacplatform.buildtools.schema.SchemaV1NavigationContent

class SchemaV1PackageNoteSelector {

    fun selectNotes(
        source: AuthorSourceData,
        navigation: SchemaV1NavigationContent
    ): List<SchemaV1PackageNote> {
        val referencedIds = buildSet {
            navigation.occasions.forEach { occasion ->
                occasion.noteId?.let { add(it) }
            }
            navigation.prayerSequences.forEach { sequence ->
                addAll(sequence.noteIds)
            }
        }
        return source.notes
            .filter {
                it.id in referencedIds &&
                    it.publicationStatus == "PUBLISHED"
            }
            .map { SchemaV1PackageNote(id = it.id, text = it.text) }
    }

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