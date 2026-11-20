package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.AuthorSourceData
import org.syriacplatform.buildtools.source.models.ExistsInTextSource

/**
 * A book/day/prayer projection for the existing Schema-v1 composition pipeline.
 * Keeps source occurrence identities, ordered items and verses intact.
 * Does not infer melodies or change the existing Occasion package builder.
 */
data class BookPrayerComposition(
    val bookId: Long,
    val dayId: Long,
    val prayerId: Long,
    val items: List<BookPrayerOccurrence>
)

data class BookPrayerOccurrence(
    val existsInId: Long,
    val sort: Long?,
    val qoloId: Long?,
    val qintoId: Long?,
    val itemType: String?,
    val verses: List<SchemaV1TextOccurrence>
)

class BookPrayerCompositionMapper(
    private val selector: BookPrayerItemSelector = BookPrayerItemSelector()
) {
    fun map(
        source: AuthorSourceData,
        bookId: Long,
        dayId: Long,
        prayerId: Long
    ): BookPrayerComposition {
        val versesByItem = source.existsInTexts.groupBy { it.existsInId }
        val petgomoByOccurrence = source.petExis.groupBy { it.existsInTextId }

        val items = selector.select(source.existsIn, bookId, dayId, prayerId)
            .map { item ->
                val verses = versesByItem[item.id].orEmpty()
                    .sortedWith(
                        compareBy<ExistsInTextSource>(
                            { it.sortInPrayer ?: Int.MAX_VALUE },
                            { it.id }
                        )
                    )
                    .map { verse ->
                        val textId = requireNotNull(verse.textId) {
                            "ExistsInText ${verse.id} has no TextID."
                        }
                        val petgomos = petgomoByOccurrence[verse.id].orEmpty()
                        require(petgomos.size <= 1) {
                            "ExistsInText ${verse.id} has multiple Petgomo assignments."
                        }
                        SchemaV1TextOccurrence(
                            id = verse.id,
                            textId = textId,
                            petgomoId = petgomos.singleOrNull()?.petgomoId
                        )
                    }
                BookPrayerOccurrence(
                    existsInId = item.id,
                    sort = item.sort,
                    qoloId = item.qoloId,
                    qintoId = item.qintoId,
                    itemType = item.itemType,
                    verses = verses
                )
            }

        return BookPrayerComposition(bookId, dayId, prayerId, items)
    }
}
