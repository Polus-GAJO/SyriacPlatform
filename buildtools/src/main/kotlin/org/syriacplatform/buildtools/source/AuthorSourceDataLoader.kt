package org.syriacplatform.buildtools.source

import java.nio.file.Files
import java.nio.file.Path

class AuthorSourceDataLoader(
    private val csvReader: CsvTableReader = CsvTableReader(),
    private val mapper: AuthorSourceMapper = AuthorSourceMapper()
) {

    fun load(directory: Path): AuthorSourceData {
        require(Files.isDirectory(directory)) {
            "Author source directory does not exist: $directory"
        }

        val occasionRows = readRows(
            directory,
            "Occasion.csv"
        )

        require(occasionRows.size == 1) {
            "Expected exactly one Occasion row, " +
                    "but found ${occasionRows.size}."
        }

        return AuthorSourceData(
            occasion = mapper.toOccasion(
                occasionRows.single()
            ),

            prayers = readRows(
                directory,
                "Prayers.csv"
            ).map(mapper::toPrayer),

            occasionLinks = readRows(
                directory,
                "OccaExis.csv"
            ).map(mapper::toOccaExis),

            existsIn = readRows(
                directory,
                "ExistsIn.csv"
            ).map(mapper::toExistsIn),

            existsInTexts = readRows(
                directory,
                "ExistsInText.csv"
            ).map(mapper::toExistsInText),

            petExis = readRows(
                directory,
                "PetExis.csv"
            ).map(mapper::toPetExis),

            qolos = readRows(
                directory,
                "Qolos.csv"
            ).map(mapper::toQolo),

            texts = readRows(
                directory,
                "Texts.csv"
            ).map(mapper::toText),

            petgomos = readRows(
                directory,
                "Petgomo.csv"
            ).map(mapper::toPetgomo),

            melodies = readRows(
                directory,
                "Melody.csv"
            ).map(mapper::toMelody),

            qintos = readRows(
                directory,
                "Qinto.csv"
            ).map(mapper::toQinto)
        )
    }

    private fun readRows(
        directory: Path,
        fileName: String
    ): List<CsvRow> {
        val path = directory.resolve(fileName)

        require(Files.isRegularFile(path)) {
            "Required Author source file was not found: $path"
        }

        return csvReader.read(path).rows
    }
}