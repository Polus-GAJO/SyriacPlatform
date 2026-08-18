package org.syriacplatform.buildtools.schema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProjectionIdFactoryTest {

    @Test
    fun prayerSequenceIdentityIsDeterministic() {
        val first =
            ProjectionIdFactory.prayerSequenceId(
                occasionId = 1L,
                prayerId = 3L
            )

        val second =
            ProjectionIdFactory.prayerSequenceId(
                occasionId = 1L,
                prayerId = 3L
            )

        assertEquals(
            first,
            second
        )

        assertEquals(
            4294967299L,
            first
        )
    }

    @Test
    fun differentPrayersProduceDifferentSequenceIds() {
        val prayer3 =
            ProjectionIdFactory.prayerSequenceId(
                occasionId = 1L,
                prayerId = 3L
            )

        val prayer4 =
            ProjectionIdFactory.prayerSequenceId(
                occasionId = 1L,
                prayerId = 4L
            )

        assertNotEquals(
            prayer3,
            prayer4
        )
    }

    @Test
    fun samePrayerInDifferentOccasionsProducesDifferentSequenceIds() {
        val occasion1 =
            ProjectionIdFactory.prayerSequenceId(
                occasionId = 1L,
                prayerId = 3L
            )

        val occasion2 =
            ProjectionIdFactory.prayerSequenceId(
                occasionId = 2L,
                prayerId = 3L
            )

        assertNotEquals(
            occasion1,
            occasion2
        )
    }

    @Test
    fun representativePrayerSequenceIdsAreStable() {
        val ids =
            listOf(
                1L,
                3L,
                4L,
                5L,
                9L,
                10L
            ).map {
                ProjectionIdFactory.prayerSequenceId(
                    occasionId = 1L,
                    prayerId = it
                )
            }

        assertEquals(
            listOf(
                4294967297L,
                4294967299L,
                4294967300L,
                4294967301L,
                4294967305L,
                4294967306L
            ),
            ids
        )
    }

    @Test
    fun occasionEntryPointIdentityIsStable() {
        assertEquals(
            1L,
            ProjectionIdFactory
                .entryPointIdForOccasion(1L)
        )
    }
}