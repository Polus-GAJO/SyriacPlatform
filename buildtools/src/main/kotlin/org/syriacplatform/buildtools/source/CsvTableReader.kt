package org.syriacplatform.buildtools.source

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class CsvTableReader {

    fun read(path: Path): CsvTable {
    val text = Files
        .readString(path, StandardCharsets.UTF_8)
        .removePrefix("\uFEFF")

    return parse(text)
}

    internal fun parse(text: String): CsvTable {
        val records = parseRecords(text)

        if (records.isEmpty()) {
            return CsvTable(
                headers = emptyList(),
                rows = emptyList()
            )
        }

        val headers = records.first()

        require(headers.isNotEmpty()) {
            "CSV header row must not be empty."
        }

        require(headers.none { it.isBlank() }) {
            "CSV header names must not be blank."
        }

        require(headers.distinct().size == headers.size) {
            "CSV header names must be unique."
        }

        val rows = records
            .drop(1)
            .filterNot { record ->
                record.size == 1 && record.first().isEmpty()
            }
            .mapIndexed { index, record ->
                require(record.size == headers.size) {
                    "CSV row ${index + 2} has ${record.size} columns; " +
                        "expected ${headers.size}."
                }

                CsvRow(
                    values = headers
                        .zip(record)
                        .associate { (header, value) ->
                            header to value.ifEmpty { null }
                        }
                )
            }

        return CsvTable(
            headers = headers,
            rows = rows
        )
    }

    private fun parseRecords(text: String): List<List<String>> {
        if (text.isEmpty()) {
            return emptyList()
        }

        val records = mutableListOf<List<String>>()
        var currentRecord = mutableListOf<String>()
        val currentField = StringBuilder()

        var index = 0
        var insideQuotes = false

        fun finishField() {
            currentRecord.add(currentField.toString())
            currentField.setLength(0)
        }

        fun finishRecord() {
            finishField()
            records.add(currentRecord)
            currentRecord = mutableListOf()
        }

        while (index < text.length) {
            val char = text[index]

            if (insideQuotes) {
                when {
                    char == '"' &&
                        index + 1 < text.length &&
                        text[index + 1] == '"' -> {

                        currentField.append('"')
                        index += 2
                    }

                    char == '"' -> {
                        insideQuotes = false
                        index++
                    }

                    else -> {
                        currentField.append(char)
                        index++
                    }
                }
            } else {
                when (char) {
                    '"' -> {
                        insideQuotes = true
                        index++
                    }

                    ',' -> {
                        finishField()
                        index++
                    }

                    '\r' -> {
                        if (
                            index + 1 < text.length &&
                            text[index + 1] == '\n'
                        ) {
                            index += 2
                        } else {
                            index++
                        }

                        finishRecord()
                    }

                    '\n' -> {
                        index++
                        finishRecord()
                    }

                    else -> {
                        currentField.append(char)
                        index++
                    }
                }
            }
        }

        require(!insideQuotes) {
            "CSV ended while inside a quoted field."
        }

        if (
            currentField.isNotEmpty() ||
            currentRecord.isNotEmpty()
        ) {
            finishRecord()
        }

        return records
    }
}