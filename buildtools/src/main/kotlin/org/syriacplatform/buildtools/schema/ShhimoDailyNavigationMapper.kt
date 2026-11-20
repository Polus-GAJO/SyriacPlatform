package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.models.ExistsInSource

/**
 * Daily Shhimo navigation only. DayN=0 belongs to separate workflows
 * (including Tashmeshto) and must never be merged into daily prayers.
 * No assumption is made about a fixed number of prayers per day.
 */
data class ShhimoDailyPrayer(
    val prayerId: Long,
    val itemCount: Int
)

data class ShhimoDailyDay(
    val dayId: Long,
    val prayers: List<ShhimoDailyPrayer>
)

class ShhimoDailyNavigationMapper {
    fun map(items: List<ExistsInSource>, bookId: Long): List<ShhimoDailyDay> {
        require(bookId > 0L) { "Book ID must be positive." }
        return items.asSequence()
            .filter { it.bookId == bookId && it.dayId in 1L..7L }
            .filter { (it.prayerId ?: 0L) > 0L }
            .groupBy { requireNotNull(it.dayId) }
            .toSortedMap()
            .map { (dayId, dayItems) ->
                ShhimoDailyDay(
                    dayId = dayId,
                    prayers = dayItems.groupBy { requireNotNull(it.prayerId) }
                        .toSortedMap()
                        .map { (prayerId, prayerItems) ->
                            ShhimoDailyPrayer(prayerId, prayerItems.size)
                        }
                )
            }
    }
}
