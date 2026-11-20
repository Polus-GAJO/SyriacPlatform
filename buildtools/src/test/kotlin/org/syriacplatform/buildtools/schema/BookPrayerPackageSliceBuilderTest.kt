package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.AuthorSourceData
import org.syriacplatform.buildtools.source.models.ExistsInSource
import org.syriacplatform.buildtools.source.models.ExistsInTextSource
import org.syriacplatform.buildtools.source.models.OccasionSource
import org.syriacplatform.buildtools.source.models.PetExisSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BookPrayerPackageSliceBuilderTest {
    @Test
    fun buildsOrderedPrayerSliceWithVersesAndPetgomo() {
        val source = AuthorSourceData(
            occasion = OccasionSource(null, 1, "Test", null, null),
            prayers = emptyList(),
            occasionLinks = emptyList(),
            existsIn = listOf(
                ExistsInSource(349, 2, 5, 1, null, 325, null, null, 1),
                ExistsInSource(7820, 1, 5, 1, null, 319, 22, null, 1),
                ExistsInSource(999, 1, 5, 1, null, 325, null, null, 2)
            ),
            existsInTexts = listOf(
                ExistsInTextSource(10, 2613, 349, 2),
                ExistsInTextSource(11, 877, 349, 1),
                ExistsInTextSource(12, 4336, 7820, 1)
            ),
            petExis = listOf(PetExisSource(5, 9, null, 10)),
            qolos = emptyList(),
            texts = emptyList(),
            petgomos = emptyList(),
            melodies = emptyList(),
            qintos = emptyList()
        )
        val slice = BookPrayerPackageSliceBuilder().build(source, 5, 1, 1, 1001)
        assertEquals(listOf(7820L, 349L), slice.sequence.liturgicalItemIds)
        assertEquals(slice.sequence.liturgicalItemIds, slice.liturgicalItems.map { it.id })
        val second = assertIs<SchemaV1QoloLiturgicalItem>(slice.liturgicalItems[1])
        assertEquals(listOf(11L, 10L), second.verses.map { it.id })
        assertEquals(9L, second.verses[1].petgomoId)
    }
}
