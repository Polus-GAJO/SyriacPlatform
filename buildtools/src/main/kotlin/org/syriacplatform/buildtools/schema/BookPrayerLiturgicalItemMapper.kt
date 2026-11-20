package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.AuthorSourceData

/**
 * Converts a book prayer projection into the existing Schema-v1
 * LiturgicalItem types. Does not select or invent a melody when ambiguous.
 *
 * PRAYER_NOTE and empty placeholders require separate publication handling;
 * they must not silently become ordinary liturgical text items.
 */
class BookPrayerLiturgicalItemMapper(
    private val melodyResolver: MelodyResolver = MelodyResolver()
) {
    fun map(
        source: AuthorSourceData,
        composition: BookPrayerComposition
    ): List<SchemaV1LiturgicalItem> {
        val sourceItems = source.existsIn.associateBy { it.id }
        val publishedNotes = source.notes
            .filter { it.publicationStatus == "PUBLISHED" }
            .map { it.id }.toSet()
        val locations = source.locations.associate { it.id to it.name }
        val qintos = source.qintos.associate { it.id to it.name }

        return composition.items.map { occurrence ->
            val item = requireNotNull(sourceItems[occurrence.existsInId]) {
                "Missing ExistsIn ${occurrence.existsInId}."
            }
            require(
                item.bookId == composition.bookId &&
                    item.dayId == composition.dayId &&
                    item.prayerId == composition.prayerId
            ) { "ExistsIn ${item.id} is outside the selected book/day/prayer." }
            require(item.itemType != "PRAYER_NOTE") {
                "ExistsIn ${item.id} is a PRAYER_NOTE; publish it as a note."
            }
            require(item.qoloId != null || occurrence.verses.isNotEmpty()) {
                "ExistsIn ${item.id} is an empty placeholder."
            }

            val noteId = item.noteId?.takeIf { it in publishedNotes }
            when (val qoloId = item.qoloId) {
                null -> SchemaV1TextLiturgicalItem(
                    id = item.id,
                    verses = occurrence.verses,
                    noteId = noteId,
                    locationTitle = item.locationId?.let(locations::get)
                )
                0L -> SchemaV1UnresolvedQoloLiturgicalItem(
                    id = item.id,
                    verses = occurrence.verses,
                    noteId = noteId
                )
                else -> {
                    val resolution = melodyResolver.resolve(item, source.melodies)
                    SchemaV1QoloLiturgicalItem(
                        id = item.id,
                        qoloId = qoloId,
                        effectiveMelodyId =
                            (resolution as? MelodyResolution.Resolved)?.melodyId,
                        melodyCandidateIds =
                            (resolution as? MelodyResolution.Ambiguous)?.melodyIds.orEmpty(),
                        verses = occurrence.verses,
                        noteId = noteId,
                        locationTitle = item.locationId?.let(locations::get),
                        qintoTitle = item.qintoId
                            ?.takeIf { it > 0L }
                            ?.let(qintos::get)
                            ?.takeIf { it.isNotBlank() }
                    )
                }
            }
        }
    }
}
