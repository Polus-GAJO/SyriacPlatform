package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import java.nio.file.Path
import org.syriacplatform.buildtools.source.AuthorSourceData
import org.syriacplatform.buildtools.source.AuthorSourceMapper
import org.syriacplatform.buildtools.source.CsvTableReader
import org.syriacplatform.buildtools.source.models.OccasionSource

/**
 * Development CLI for the complete seven-day Shhimo Book package.
 *
 * Expected CSVs: ExistsIn, ExistsInText, PetExis, Prayers, Qolos, Texts,
 * Petgomo, Melody, Qinto. Optional files may be absent. No Occasion or
 * OccaExis export is needed for this book-oriented workflow.
 *
 * Usage: <bookId> <sourceDirectory> <outputDirectory>
 */
fun main(args: Array<String>) {
    require(args.size == 3) {
        "Usage: <bookId> <dayId> <prayerId> <sourceDirectory> <outputDirectory>"
    }
    val bookId = args[0].toLong()
    require(bookId > 0)
    val sourceDirectory = Path.of(args[1]).toAbsolutePath().normalize()
    val outputDirectory = Path.of(args[2]).toAbsolutePath().normalize()
    require(Files.isDirectory(sourceDirectory))
    require(sourceDirectory != outputDirectory)
    require(!outputDirectory.startsWith(sourceDirectory)) {
        "Output directory must not be inside the source directory."
    }

    val mapper = AuthorSourceMapper()
    val csv = CsvTableReader()
    fun rows(file: String) = sourceDirectory.resolve(file).let { path ->
        if (Files.isRegularFile(path)) csv.read(path).rows else emptyList()
    }
    val source = AuthorSourceData(
        occasion = OccasionSource(null, 1L, "Book preview", null, null),
        prayers = rows("Prayers.csv").map(mapper::toPrayer),
        occasionLinks = emptyList(),
        existsIn = rows("ExistsIn.csv").map(mapper::toExistsIn),
        existsInTexts = rows("ExistsInText.csv").map(mapper::toExistsInText),
        petExis = rows("PetExis.csv").map(mapper::toPetExis),
        qolos = rows("Qolos.csv").map(mapper::toQolo),
        texts = rows("Texts.csv").map(mapper::toText),
        petgomos = rows("Petgomo.csv").map(mapper::toPetgomo),
        melodies = rows("Melody.csv").map(mapper::toMelody),
        qintos = rows("Qinto.csv").map(mapper::toQinto),
        locations = rows("Location.csv").map(mapper::toLocation),
        notes = rows("Notes.csv").map(mapper::toNote),
        languages = rows("Languages.csv").map(mapper::toLanguage),
        translations = rows("Translations.csv").map(mapper::toTranslation),
        translationLinks = rows("TranslationLinks.csv").map(mapper::toTranslationLink)
    )
    val packageData = ShhimoDailyPackageBuilder().build(source, bookId)
    if (Files.exists(outputDirectory)) {
        require(Files.isDirectory(outputDirectory)) { "Output is not a directory." }
        require(Files.list(outputDirectory).use { !it.findAny().isPresent }) {
            "Output directory must be empty to prevent accidental data loss."
        }
    }
    ShhimoDailyPackageWriter().write(bookId, packageData, outputDirectory)
    println("Shhimo daily package written to $outputDirectory")
    println("Days: ${packageData.days.size}")
    println("Prayer sequences: ${packageData.content.prayerSequences.size}")
    println("Liturgical items: ${packageData.content.liturgicalItems.size}")
    println("Text occurrences: ${packageData.content.liturgicalItems.sumOf { it.verses.size }}")
}
