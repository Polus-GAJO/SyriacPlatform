package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.models.ExistsInSource

/**
 * Selects one book/day/prayer context without assuming that all days have
 * the same prayers or that every item has a Qolo, Qinto, or Melody.
 *
 * The source ordering is preserved by ExistsIn.Sort. The ordered verses
 * within each selected item are mapped separately from ExistsInText.SortInPra.
 */
class BookPrayerItemSelector {
    fun select(
        items: List<ExistsInSource>,
        bookId: Long,
        dayId: Long,
        prayerId: Long
    ): List<ExistsInSource> =
        items.asSequence()
            .filter {
                it.bookId == bookId &&
                    it.dayId == dayId &&
                    it.prayerId == prayerId
            }
            .sortedWith(
                compareBy<ExistsInSource>(
                    { it.sort ?: Long.MAX_VALUE },
                    { it.id }
                )
            )
            .toList()
}
