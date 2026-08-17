package org.syriacplatform.buildtools.source

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthorSourceMapperTest {

    private val reader = CsvTableReader()
    private val mapper = AuthorSourceMapper()

    @Test
    fun mapsRepresentativeOccasion() {
        val table = reader.read(
            representativeExportPath("Occasion.csv")
        )

        val occasion = mapper.toOccasion(
            table.rows.single()
        )

        assertEquals(158L, occasion.sort)
        assertEquals(1L, occasion.id)
        assertEquals(
            "ܩܽܘܕܳܫ ܥܺܕܬܳܐ",
            occasion.name
        )
        assertNull(occasion.day)
        assertNull(occasion.monthId)
    }

    @Test
    fun mapsRepresentativePrayers() {
        val table = reader.read(
            representativeExportPath("Prayers.csv")
        )

        val prayers = table.rows.map(
            mapper::toPrayer
        )

        assertEquals(6, prayers.size)

        assertEquals(
            listOf(
                1L,
                3L,
                4L,
                5L,
                9L,
                10L
            ),
            prayers.map { it.id }
        )
    }

    @Test
    fun mapsRepresentativeExistsInRows() {
        val table = reader.read(
            representativeExportPath("ExistsIn.csv")
        )

        val items = table.rows.map(
            mapper::toExistsIn
        )

        assertEquals(52, items.size)

        val prayerIds = items
            .mapNotNull { it.prayerId }
            .distinct()
            .sorted()

        assertEquals(
            listOf(
                1L,
                3L,
                4L,
                5L,
                9L,
                10L
            ),
            prayerIds
        )
    }

    @Test
    fun preservesUndeterminedQintoZero() {
        val table = reader.read(
            representativeExportPath("ExistsIn.csv")
        )

        val items = table.rows.map(
            mapper::toExistsIn
        )

        val unresolvedQintoItems =
            items.filter { it.qintoId == 0L }

        assertEquals(
            13,
            unresolvedQintoItems.size
        )
    }

    @Test
    fun mapsEmptyValuesToNull() {
        val row = CsvRow(
            values = mapOf(
                "ID" to "100",
                "Sort" to "1",
                "BookN" to null,
                "PrayerN" to "5",
                "LocationN" to null,
                "QoloN" to "319",
                "QintoN" to null,
                "NoteN" to null,
                "DayN" to null
            )
        )

        val item = mapper.toExistsIn(row)

        assertEquals(100L, item.id)
        assertEquals(1L, item.sort)
        assertEquals(5L, item.prayerId)
        assertEquals(319L, item.qoloId)

        assertNull(item.bookId)
        assertNull(item.locationId)
        assertNull(item.qintoId)
        assertNull(item.noteId)
        assertNull(item.dayId)
    }

    private fun representativeExportPath(
        fileName: String
    ): Path {
        return Path.of(
            "..",
            "author-database",
            "samples",
            "mapping-analysis",
            fileName
        ).toAbsolutePath().normalize()
    }
}