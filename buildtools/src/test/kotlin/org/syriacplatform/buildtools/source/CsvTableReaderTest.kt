package org.syriacplatform.buildtools.source

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CsvTableReaderTest {

    private val reader = CsvTableReader()

    @Test
    fun readsRepresentativeOccasionExport() {
        val path = representativeExportPath("Occasion.csv")

        assertTrue(
            path.exists(),
            "Representative export was not found: $path"
        )

        val table = reader.read(path)

        assertEquals(
    listOf(
        "OccSort",
        "OccN",
        "Occasion",
        "OccDay",
        "OccMonth"
    ),
    table.headers
)

        assertEquals(1, table.rows.size)

        val row = table.rows.single()

        assertEquals("1", row["OccN"])
        assertEquals(
            "ܩܽܘܕܳܫ ܥܺܕܬܳܐ",
            row["Occasion"]
        )
    }

    @Test
    fun parsesQuotedCommasAndEscapedQuotes() {
        val csv = """
            id,text,note
            "1","ܫܠܳܡܳܐ, ܥܰܡܟܽܘܢ","He said ""test""."
        """.trimIndent()

        val table = reader.parse(csv)

        assertEquals(1, table.rows.size)

        val row = table.rows.single()

        assertEquals("1", row["id"])
        assertEquals(
            "ܫܠܳܡܳܐ, ܥܰܡܟܽܘܢ",
            row["text"]
        )
        assertEquals(
            "He said \"test\".",
            row["note"]
        )
    }

    @Test
    fun convertsEmptyCsvFieldsToNull() {
        val csv = """
            id,value
            "1",
        """.trimIndent()

        val table = reader.parse(csv)

        assertEquals(null, table.rows.single()["value"])
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