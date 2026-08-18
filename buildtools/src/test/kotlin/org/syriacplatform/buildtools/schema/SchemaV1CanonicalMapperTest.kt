package org.syriacplatform.buildtools.schema

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader
import org.syriacplatform.buildtools.validation.SourceValidator

class SchemaV1CanonicalMapperTest {

    private val loader = AuthorSourceDataLoader()
    private val validator = SourceValidator()
    private val mapper = SchemaV1CanonicalMapper()

    @Test
    fun mapsRepresentativeCanonicalContent() {
        val source = loadValidatedSource()

        val content = mapper.map(source)

        assertEquals(
            6,
            content.prayers.size
        )

        assertTrue(
            content.qolos.isNotEmpty()
        )

        assertTrue(
            content.texts.isNotEmpty()
        )

        assertTrue(
            content.petgomos.isNotEmpty()
        )

        assertTrue(
            content.qintos.isNotEmpty()
        )

        assertTrue(
            content.melodies.isNotEmpty()
        )
    }

    @Test
    fun preservesCanonicalSourceIdentifiers() {
        val source = loadValidatedSource()

        val content = mapper.map(source)

        assertEquals(
            source.prayers.map { it.id },
            content.prayers.map { it.id }
        )

        assertEquals(
            source.qolos.map { it.id },
            content.qolos.map { it.id }
        )

        assertEquals(
            source.texts.map { it.id },
            content.texts.map { it.id }
        )

        assertEquals(
            source.petgomos.map { it.id },
            content.petgomos.map { it.id }
        )

        assertEquals(
            source.melodies.map { it.id },
            content.melodies.map { it.id }
        )
    }

    @Test
    fun unresolvedQintoZeroDoesNotBecomeCanonicalContent() {
        val source = loadValidatedSource()

        assertTrue(
            source.qintos.any { it.id == 0L }
        )

        val content = mapper.map(source)

        assertFalse(
            content.qintos.any { it.id == 0L }
        )
    }

    @Test
    fun mapsPrayerNamesFromAuthorDatabase() {
        val source = loadValidatedSource()

        val content = mapper.map(source)

        assertEquals(
            source.prayers.map { it.name },
            content.prayers.map { it.name }
        )
    }

    @Test
    fun preservesMultipleMelodiesWithoutSelectingOne() {
        val source = loadValidatedSource()

        val content = mapper.map(source)

        val candidates = content.melodies.filter {
            it.qoloId == 116L
        }

        assertTrue(
            candidates.any { it.id == 119L }
        )

        assertTrue(
            candidates.any { it.id == 1965L }
        )
    }

    @Test
    fun melodyWithoutSearchNameFallsBackToMelodyName() {
        val source = loadValidatedSource()

        val sourceMelody = source.melodies
            .first { it.id == 2057L }

        assertTrue(
            sourceMelody.searchName == null
        )

        val content = mapper.map(source)

        val mappedMelody = content.melodies
            .first { it.id == 2057L }

        assertEquals(
            sourceMelody.name,
            mappedMelody.searchName
        )
    }

    private fun loadValidatedSource() =
        loader.load(
            representativeExportDirectory()
        ).also { source ->
            val report = validator.validate(source)

            check(!report.hasErrors) {
                report.errors.joinToString("\n") {
                    "${it.code}: ${it.message}"
                }
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