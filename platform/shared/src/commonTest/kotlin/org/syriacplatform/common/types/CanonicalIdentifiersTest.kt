package org.syriacplatform.common.types

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CanonicalIdentifiersTest {

    @Test
    fun identifiersPreserveTheirValues() {
        assertEquals(438L, QoloId(438).value)
        assertEquals(75L, MelodyId(75).value)
        assertEquals(8L, QintoId(8).value)
        assertEquals(100L, TextId(100).value)
        assertEquals(25L, OccasionId(25).value)
    }

    @Test
    fun identifiersOfTheSameTypeUseValueEquality() {
        assertEquals(
            QoloId(438),
            QoloId(438)
        )

        assertNotEquals(
            QoloId(438),
            QoloId(439)
        )
    }

    @Test
    fun identifiersImplementPlatformId() {
        val identifiers: List<PlatformId> = listOf(
            EntryPointId(1),
            OccasionId(2),
            PrayerId(3),
            PrayerSequenceId(4),
            LiturgicalItemId(5),
            TextId(6),
            PetgomoId(7),
            QoloId(8),
            MelodyId(9),
            QintoId(10),
            LocationId(11),
            GroupId(12),
            MediaAssetId(13)
        )

        assertEquals(
            (1L..13L).toList(),
            identifiers.map { it.value }
        )
    }
}