package org.syriacplatform.buildtools.source

data class CsvTable(
    val headers: List<String>,
    val rows: List<CsvRow>
)

data class CsvRow(
    val values: Map<String, String?>
) {
    operator fun get(columnName: String): String? =
        values[columnName]
}