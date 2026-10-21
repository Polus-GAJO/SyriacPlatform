package org.syriacplatform.buildtools.source

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.test.assertFailsWith

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
    fun loadsSyriacLocationTitles() {
        val temporaryDirectory = Files.createTempDirectory("syriac-location-test-")
        try {
            Files.list(representativeExportDirectory()).use { files ->
                files.filter { Files.isRegularFile(it) }.forEach { file ->
                    Files.copy(file, temporaryDirectory.resolve(file.fileName))
                }
            }
            Files.writeString(
                temporaryDirectory.resolve("Location.csv"),
                "LocationN,Location\n11,ܣܶܕܪܳܐ\n"
            )
            val data = loader.load(temporaryDirectory)
            assertEquals(1, data.locations.size)
            assertEquals(11L, data.locations.single().id)
            assertEquals("ܣܶܕܪܳܐ", data.locations.single().name)
        } finally {
            temporaryDirectory.toFile().deleteRecursively()
        }
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

    @Test
fun loadsPublishedNotesFromCsv() {
    val temporaryDirectory = Files.createTempDirectory(
        "syriac-notes-test-"
    )

    try {
        val sourceDirectory = representativeExportDirectory()

        Files.list(sourceDirectory).use { files ->
            files.filter { Files.isRegularFile(it) }
                .forEach { sourceFile ->
                    Files.copy(
                        sourceFile,
                        temporaryDirectory.resolve(sourceFile.fileName),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
        }

        Files.writeString(
            temporaryDirectory.resolve("Notes.csv"),
            """
            NoteN,Note,publicationStatus
            1001,Published test note,PUBLISHED
            """.trimIndent() + "\n"
        )

        val data = loader.load(temporaryDirectory)

        assertEquals(1, data.notes.size)
        assertEquals(1001L, data.notes.single().id)
        assertEquals(
            "Published test note",
            data.notes.single().text
        )
        assertEquals(
            "PUBLISHED",
            data.notes.single().publicationStatus
        )
    } finally {
        temporaryDirectory.toFile().deleteRecursively()
    }
}

@Test
fun rejectsArchivedNotesFromCsv() {
    val temporaryDirectory = Files.createTempDirectory(
        "syriac-archived-notes-test-"
    )

    try {
        val sourceDirectory = representativeExportDirectory()

        Files.list(sourceDirectory).use { files ->
            files.filter { Files.isRegularFile(it) }
                .forEach { sourceFile ->
                    Files.copy(
                        sourceFile,
                        temporaryDirectory.resolve(sourceFile.fileName),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
        }

        Files.writeString(
            temporaryDirectory.resolve("Notes.csv"),
            """
            NoteN,Note,publicationStatus
            1002,Archived test note,ARCHIVE
            """.trimIndent() + "\n"
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            loader.load(temporaryDirectory)
        }

        assertTrue(
            exception.message?.contains(
                "Notes.csv contains notes that are not PUBLISHED."
            ) == true
        )
    } finally {
        temporaryDirectory.toFile().deleteRecursively()
    }
}


    @Test
    fun loadsPublishedTranslationsAndSharedLinks() {
        val original = loader.load(representativeExportDirectory())
        val textId = original.texts.first().id
        val petId = original.petgomos.first().id
        val temp = Files.createTempDirectory("translation-loader-")
        try {
            Files.list(representativeExportDirectory()).use { stream ->
                stream.filter { Files.isRegularFile(it) }.forEach {
                    Files.copy(it, temp.resolve(it.fileName), StandardCopyOption.REPLACE_EXISTING)
                }
            }
            Files.writeString(temp.resolve("Languages.csv"), "LanguageID,LanguageCode,LanguageName\n1,ar,Arabic\n")
            Files.writeString(temp.resolve("Translations.csv"), "TranslationID,LanguageID,Translation,Translator,Source,ReviewStatus\n41,1,Example,,,Draft\n")
            Files.writeString(temp.resolve("TranslationLinks.csv"), "LinkID,TranslationID,TextID,PetN,IsPublished\n1,41,$textId,,True\n2,41,,$petId,True\n")
            val data = loader.load(temp)
            assertEquals("ar", data.languages.single().code)
            assertEquals("Example", data.translations.single().content)
            assertEquals(2, data.translationLinks.size)
            assertEquals(textId, data.translationLinks[0].textId)
            assertEquals(petId, data.translationLinks[1].petgomoId)
        } finally {
            temp.toFile().deleteRecursively()
        }
    }

    @Test
    fun rejectsUnpublishedTranslationLinkInExport() {
        val temp = Files.createTempDirectory("translation-unpublished-")
        try {
            Files.list(representativeExportDirectory()).use { stream ->
                stream.filter { Files.isRegularFile(it) }.forEach {
                    Files.copy(it, temp.resolve(it.fileName), StandardCopyOption.REPLACE_EXISTING)
                }
            }
            Files.writeString(temp.resolve("Languages.csv"), "LanguageID,LanguageCode,LanguageName\n1,ar,Arabic\n")
            Files.writeString(temp.resolve("Translations.csv"), "TranslationID,LanguageID,Translation,Translator,Source,ReviewStatus\n41,1,Example,,,Draft\n")
            Files.writeString(temp.resolve("TranslationLinks.csv"), "LinkID,TranslationID,TextID,PetN,IsPublished\n1,41,100,,False\n")
            assertFailsWith<IllegalArgumentException> { loader.load(temp) }
        } finally {
            temp.toFile().deleteRecursively()
        }
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