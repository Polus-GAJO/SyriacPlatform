package org.syriacplatform.buildtools.source

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthorSourceDataLoaderTest {

    private val loader = AuthorSourceDataLoader()

    @Test
    fun loadsRepresentativeAuthorSourceData() {
        val data = loader.load(
            representativeExportDirectory()
        )

        assertEquals(
            1L,
            data.occasion.id
        )

        assertEquals(
            "ܩܽܘܕܳܫ ܥܺܕܬܳܐ",
            data.occasion.name
        )

        assertEquals(
            6,
            data.prayers.size
        )

        assertEquals(
            52,
            data.occasionLinks.size
        )

        assertEquals(
            52,
            data.existsIn.size
        )

        assertTrue(
            data.existsInTexts.isNotEmpty()
        )

        assertTrue(
            data.petExis.isNotEmpty()
        )

        assertTrue(
            data.qolos.isNotEmpty()
        )

        assertTrue(
            data.texts.isNotEmpty()
        )

        assertTrue(
            data.petgomos.isNotEmpty()
        )

        assertTrue(
            data.melodies.isNotEmpty()
        )

        assertTrue(
            data.qintos.isNotEmpty()
        )
    }

    @Test
    fun preservesRepresentativePrayerOrderByPrayerId() {
        val data = loader.load(
            representativeExportDirectory()
        )

        assertEquals(
            listOf(
                1L,
                3L,
                4L,
                5L,
                9L,
                10L
            ),
            data.prayers.map { it.id }
        )
    }

    @Test
    fun preservesKnownSourceStates() {
        val data = loader.load(
            representativeExportDirectory()
        )

        assertTrue(
            data.existsIn.any {
                it.qintoId == 0L
            }
        )

        assertTrue(
            data.melodies.count {
                it.qoloId == 116L &&
                        it.qintoId == 1L
            } == 2
        )
    }

    private fun representativeExportDirectory(): Path {
        return Path.of(
            "..",
            "author-database",
            "samples",
            "mapping-analysis"
        ).toAbsolutePath().normalize()
    }
}