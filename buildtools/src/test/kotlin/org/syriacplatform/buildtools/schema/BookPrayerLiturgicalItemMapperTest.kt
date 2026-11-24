package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.AuthorSourceData
import org.syriacplatform.buildtools.source.models.ExistsInSource
import org.syriacplatform.buildtools.source.models.OccasionSource
import org.syriacplatform.buildtools.source.models.MelodySource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class BookPrayerLiturgicalItemMapperTest {
    private fun source(items: List<ExistsInSource>, melodies: List<MelodySource> = emptyList()) =
        AuthorSourceData(
            occasion = OccasionSource(null, 1, "Test", null, null),
            prayers = emptyList(),
            occasionLinks = emptyList(),
            existsIn = items,
            existsInTexts = emptyList(),
            petExis = emptyList(),
            qolos = emptyList(),
            texts = emptyList(),
            petgomos = emptyList(),
            melodies = melodies,
            qintos = emptyList()
        )

    private fun item(id: Long, qolo: Long?, qinto: Long? = null, type: String? = null) =
        ExistsInSource(id, id, 5, 1, null, qolo, qinto, null, 1, type)

    private fun occurrence(id: Long, qolo: Long?, verses: List<SchemaV1TextOccurrence>) =
        BookPrayerOccurrence(id, id, qolo, null, null, verses)

    @Test
    fun mapsTextAndQoloWithoutInventingMelody() {
        val verse = SchemaV1TextOccurrence(77, 4336, 9)
        val data = source(listOf(item(1, null), item(2, 325)))
        val composition = BookPrayerComposition(
            5, 1, 1,
            listOf(occurrence(1, null, listOf(verse)), occurrence(2, 325, listOf(verse)))
        )
        val mapped = BookPrayerLiturgicalItemMapper().map(data, composition)
        assertIs<SchemaV1TextLiturgicalItem>(mapped[0])
        val qolo = assertIs<SchemaV1QoloLiturgicalItem>(mapped[1])
        assertEquals(null, qolo.effectiveMelodyId)
        assertEquals(9L, qolo.verses.single().petgomoId)
    }

    @Test
    fun resolvesOnlyUnambiguousMelody() {
        val data = source(
            listOf(item(2, 325, 9)),
            listOf(MelodySource(88, 325, null, null, 9, null, null, null))
        )
        val mapped = BookPrayerLiturgicalItemMapper().map(
            data, BookPrayerComposition(5, 1, 1, listOf(occurrence(2, 325, emptyList())))
        )
        assertEquals(88L, assertIs<SchemaV1QoloLiturgicalItem>(mapped.single()).effectiveMelodyId)
    }

    @Test
    fun weeklyQintoIncludesOnlyEightOrdinaryTones() {
        val data = source(
            listOf(item(2, 325, 22)),
            (1L..8L).map { MelodySource(100 + it, 325, null, null, it, null, null, null) } +
                listOf(9L, 10L, 22L).map { MelodySource(100 + it, 325, null, null, it, null, null, null) }
        )
        val mapped = assertIs<SchemaV1QoloLiturgicalItem>(
            BookPrayerLiturgicalItemMapper().map(
                data, BookPrayerComposition(5, 1, 1, listOf(occurrence(2, 325, emptyList())))
            ).single()
        )
        assertEquals(true, mapped.usesWeeklyQinto)
        assertEquals(null, mapped.effectiveMelodyId)
        assertEquals((101L..108L).toList(), mapped.melodyCandidateIds)
    }

    @Test
    fun weeklyQintoDoesNotFixEvenASingleCandidate() {
        val data = source(
            listOf(item(2, 325, 22)),
            listOf(MelodySource(88, 325, null, null, 3, null, null, null))
        )
        val mapped = assertIs<SchemaV1QoloLiturgicalItem>(
            BookPrayerLiturgicalItemMapper().map(
                data, BookPrayerComposition(5, 1, 1, listOf(occurrence(2, 325, emptyList())))
            ).single()
        )
        assertEquals(true, mapped.usesWeeklyQinto)
        assertEquals(null, mapped.effectiveMelodyId)
        assertEquals(listOf(88L), mapped.melodyCandidateIds)
    }

    @Test
    fun doesNotPublishPrayerNoteAsText() {
        val data = source(listOf(item(3, null, type = "PRAYER_NOTE")))
        assertFailsWith<IllegalArgumentException> {
            BookPrayerLiturgicalItemMapper().map(
                data, BookPrayerComposition(5, 1, 1, listOf(occurrence(3, null, emptyList())))
            )
        }
    }
}
