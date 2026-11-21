package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.models.ExistsInSource

/**
 * Stable daily prayer sequence plan for Shhimo.
 *
 * Sequence IDs must be unique across days; PrayerN is reused every day.
 * IDs are assigned in sorted day/prayer order, independently of CSV row order.
 * DayN=0 is deliberately excluded and remains available for separate services.
 */
data class ShhimoDailySequencePlan(
    val dayId: Long,
    val prayerId: Long,
    val sequenceId: Long,
    val itemCount: Int
)

class ShhimoDailySequencePlanner(
    private val navigationMapper: ShhimoDailyNavigationMapper = ShhimoDailyNavigationMapper()
) {
    fun plan(items: List<ExistsInSource>, bookId: Long): List<ShhimoDailySequencePlan> {
        var nextSequenceId = 1L
        return navigationMapper.map(items, bookId).flatMap { day ->
            day.prayers.map { prayer ->
                ShhimoDailySequencePlan(
                    dayId = day.dayId,
                    prayerId = prayer.prayerId,
                    sequenceId = nextSequenceId++,
                    itemCount = prayer.itemCount
                )
            }
        }
    }
}
