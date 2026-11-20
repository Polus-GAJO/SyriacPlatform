package org.syriacplatform.buildtools.schema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookPrayerSequenceMapperTest {
    @Test
    fun preservesVerifiedPrayerItemOrder() {
        val composition = BookPrayerComposition(
            bookId = 5,
            dayId = 1,
            prayerId = 1,
            items = listOf(7820L, 349L, 9042L, 350L).mapIndexed { index, id ->
                BookPrayerOccurrence(
                    existsInId = id,
                    sort = (index + 1).toLong(),
                    qoloId = null,
                    qintoId = null,
                    itemType = null,
                    verses = emptyList()
                )
            }
        )
        val sequence = BookPrayerSequenceMapper().map(composition, sequenceId = 1001L)
        assertEquals(1001L, sequence.id)
        assertEquals(1L, sequence.prayerId)
        assertEquals(listOf(7820L, 349L, 9042L, 350L), sequence.liturgicalItemIds)
    }

    @Test
    fun rejectsDuplicateOccurrenceIdentity() {
        val occurrence = BookPrayerOccurrence(349, 1, null, null, null, emptyList())
        assertFailsWith<IllegalArgumentException> {
            BookPrayerSequenceMapper().map(
                BookPrayerComposition(5, 1, 1, listOf(occurrence, occurrence)),
                1001
            )
        }
    }
}
