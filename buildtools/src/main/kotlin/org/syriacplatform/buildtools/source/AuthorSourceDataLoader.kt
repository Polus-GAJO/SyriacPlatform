package org.syriacplatform.buildtools.source

import java.nio.file.Files
import java.nio.file.Path

class AuthorSourceDataLoader(
    private val csvReader: CsvTableReader = CsvTableReader(),
    private val mapper: AuthorSourceMapper = AuthorSourceMapper(),
    private val translationValidator: PublishedTranslationValidator = PublishedTranslationValidator()
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

        val translationFiles = listOf("Languages.csv", "Translations.csv", "TranslationLinks.csv")
        val present = translationFiles.filter { Files.isRegularFile(directory.resolve(it)) }
        require(present.isEmpty() || present.size == translationFiles.size) {
            "Translation export requires all three CSV files or none; found: $present"
        }

        val source = AuthorSourceData(
            languages = if (present.isNotEmpty()) readRows(directory, "Languages.csv").map(mapper::toLanguage) else emptyList(),
            translations = if (present.isNotEmpty()) readRows(directory, "Translations.csv").map(mapper::toTranslation) else emptyList(),
            translationLinks = if (present.isNotEmpty()) readRows(directory, "TranslationLinks.csv").map(mapper::toTranslationLink) else emptyList(),

            occasion = mapper.toOccasion(
                occasionRows.single()
            ),

            locations = directory.resolve("Location.csv").let { path ->
                if (Files.isRegularFile(path)) {
                    readRows(directory, "Location.csv").map(mapper::toLocation)
                } else {
                    emptyList()
                }
            },

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
).map(mapper::toQinto),

notes = directory.resolve("Notes.csv").let { notesPath ->
    if (Files.isRegularFile(notesPath)) {
        readRows(directory, "Notes.csv")
            .map(mapper::toNote)
            .also { notes ->
                require(notes.all { it.publicationStatus == "PUBLISHED" }) {
                    "Notes.csv contains notes that are not PUBLISHED."
                }
            }
    } else {
        emptyList()
    }
}
        )
        translationValidator.validate(source)
        return source
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