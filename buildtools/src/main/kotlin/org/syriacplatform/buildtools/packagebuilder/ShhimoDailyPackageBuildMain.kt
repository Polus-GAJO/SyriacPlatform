package org.syriacplatform.buildtools.packagebuilder

import java.nio.file.Files
import java.nio.file.Path
import org.syriacplatform.buildtools.source.AuthorSourceData
import org.syriacplatform.buildtools.source.AuthorSourceMapper
import org.syriacplatform.buildtools.source.MediaSourceDataLoader
import org.syriacplatform.buildtools.schema.SchemaV1MediaMapper
import org.syriacplatform.buildtools.schema.SchemaV1CanonicalMedia
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
    require(args.size == 3 || args.size == 5) {
        "Usage: <bookId> <sourceDirectory> <outputDirectory> OR <bookId> <sourceDirectory> <mediaSourceDirectory> <mediaLibraryRoot> <outputDirectory>"
    }
    val bookId = args[0].toLong()
    require(bookId > 0)
    val sourceDirectory = Path.of(args[1]).toAbsolutePath().normalize()
    val mediaSourceDirectory = if (args.size == 5) Path.of(args[2]).toAbsolutePath().normalize() else null
    val mediaLibraryRoot = if (args.size == 5) Path.of(args[3]).toAbsolutePath().normalize() else null
    val outputDirectory = Path.of(args.last()).toAbsolutePath().normalize()
    require(Files.isDirectory(sourceDirectory))
    require(sourceDirectory != outputDirectory)
    require(!outputDirectory.startsWith(sourceDirectory)) {
        "Output directory must not be inside the source directory."
    }

    if (mediaSourceDirectory != null) {
        require(Files.isDirectory(mediaSourceDirectory)) { "Media CSV directory missing: $mediaSourceDirectory" }
        require(Files.isDirectory(requireNotNull(mediaLibraryRoot))) { "Media library missing: $mediaLibraryRoot" }
        require(!outputDirectory.startsWith(mediaSourceDirectory))
        require(!outputDirectory.startsWith(mediaLibraryRoot))
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
    val dayNames = rows("Days.csv").associate { row ->
        val id = requireNotNull(row.values["DayN"]).toLong()
        id to requireNotNull(row.values["DayName"]) { "Missing DayName for day $id" }
    }
    val media = if (mediaSourceDirectory != null)
        SchemaV1MediaMapper().map(MediaSourceDataLoader().load(mediaSourceDirectory))
    else SchemaV1CanonicalMedia(emptyList(), emptyList())
    val packageData = ShhimoDailyPackageBuilder().build(source, bookId, media, dayNames)
    val selectedSourceMedia = packageData.content.mediaAssets.map { packaged ->
        media.mediaAssets.single { it.id == packaged.id }
    }
    if (Files.exists(outputDirectory)) {
        require(Files.isDirectory(outputDirectory)) { "Output is not a directory." }
        require(Files.list(outputDirectory).use { !it.findAny().isPresent }) {
            "Output directory must be empty to prevent accidental data loss."
        }
    }
    ShhimoDailyPackageWriter().write(bookId, packageData, outputDirectory, mediaLibraryRoot, selectedSourceMedia)
    println("Shhimo daily package written to $outputDirectory")
    println("Days: ${packageData.days.size}")
    println("Prayer sequences: ${packageData.content.prayerSequences.size}")
    println("Liturgical items: ${packageData.content.liturgicalItems.size}")
    println("Text occurrences: ${packageData.content.liturgicalItems.sumOf { it.verses.size }}")
    println("Performance recordings: ${packageData.content.performanceMedia.size}")
    println("Melody media links: ${packageData.content.melodyMedia.size}")
    println("Packaged media files: ${packageData.content.mediaAssets.size}")
}
