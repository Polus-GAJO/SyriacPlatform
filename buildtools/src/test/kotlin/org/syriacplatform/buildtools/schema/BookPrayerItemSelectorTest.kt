package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.models.ExistsInSource
import kotlin.test.Test
import kotlin.test.assertEquals

class BookPrayerItemSelectorTest {
    private fun item(
        id: Long,
        sort: Long?,
        book: Long? = 5,
        day: Long? = 1,
        prayer: Long? = 1,
        qolo: Long? = null
    ) = ExistsInSource(
        id = id,
        sort = sort,
        bookId = book,
        prayerId = prayer,
        locationId = null,
        qoloId = qolo,
        qintoId = null,
        noteId = null,
        dayId = day
    )

    @Test
    fun selectsOnlyRequestedContextAndPreservesSourceOrdering() {
        val items = listOf(
            item(350, 4, qolo = 203),
            item(9042, 3, qolo = 319),
            item(349, 2),
            item(7820, 1, qolo = 319),
            item(999, 1, day = 2),
            item(998, 1, prayer = 2),
            item(997, 1, book = 6)
        )
        assertEquals(
            listOf(7820L, 349L, 9042L, 350L),
            BookPrayerItemSelector().select(items, 5, 1, 1).map { it.id }
        )
    }

    @Test
    fun breaksEqualSortTiesByIdAndLeavesMissingSortLast() {
        val items = listOf(item(9, null), item(7, 2), item(3, 2))
        assertEquals(
            listOf(3L, 7L, 9L),
            BookPrayerItemSelector().select(items, 5, 1, 1).map { it.id }
        )
    }
}
