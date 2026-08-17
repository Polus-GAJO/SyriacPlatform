package org.syriacplatform.buildtools.source

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthorSourceRelationshipsTest {

    private val reader = CsvTableReader()
    private val mapper = AuthorSourceMapper()

    @Test
    fun occasionLinksResolveToExportedExistsInRows() {
        val occasionLinks = readRows("OccaExis.csv")
            .map(mapper::toOccaExis)

        val existsInIds = readRows("ExistsIn.csv")
            .map(mapper::toExistsIn)
            .map { it.id }
            .toSet()

        assertTrue(occasionLinks.isNotEmpty())

        occasionLinks.forEach { link ->
            assertEquals(1L, link.occasionId)

            val existsInId = assertNotNull(
                link.existsInId
            )

            assertTrue(
                existsInId in existsInIds,
                "OccaExis ${link.id} references " +
                        "missing ExistsIn $existsInId."
            )
        }
    }

    @Test
    fun contextualTextReferencesResolve() {
        val existsInIds = readRows("ExistsIn.csv")
            .map(mapper::toExistsIn)
            .map { it.id }
            .toSet()

        val textIds = readRows("Texts.csv")
            .map(mapper::toText)
            .map { it.id }
            .toSet()

        val occurrences = readRows("ExistsInText.csv")
            .map(mapper::toExistsInText)

        assertTrue(occurrences.isNotEmpty())

        occurrences.forEach { occurrence ->
            val existsInId = assertNotNull(
                occurrence.existsInId
            )

            val textId = assertNotNull(
                occurrence.textId
            )

            assertTrue(
                existsInId in existsInIds,
                "ExistsInText ${occurrence.id} references " +
                        "missing ExistsIn $existsInId."
            )

            assertTrue(
                textId in textIds,
                "ExistsInText ${occurrence.id} references " +
                        "missing Text $textId."
            )
        }
    }

    @Test
    fun petgomoContextReferencesResolveAndAgreeWithText() {
        val occurrences = readRows("ExistsInText.csv")
            .map(mapper::toExistsInText)
            .associateBy { it.id }

        val petgomoIds = readRows("Petgomo.csv")
            .map(mapper::toPetgomo)
            .map { it.id }
            .toSet()

        val assignments = readRows("PetExis.csv")
            .map(mapper::toPetExis)

        assertTrue(assignments.isNotEmpty())

        assignments.forEach { assignment ->
            val occurrenceId = assertNotNull(
                assignment.existsInTextId
            )

            val occurrence = assertNotNull(
                occurrences[occurrenceId],
                "PetExis ${assignment.id} references " +
                        "missing ExistsInText $occurrenceId."
            )

            val petgomoId = assertNotNull(
                assignment.petgomoId
            )

            assertTrue(
                petgomoId in petgomoIds,
                "PetExis ${assignment.id} references " +
                        "missing Petgomo $petgomoId."
            )

            if (assignment.textId != null) {
                assertEquals(
                    occurrence.textId,
                    assignment.textId,
                    "PetExis ${assignment.id} TextID does not " +
                            "match ExistsInText $occurrenceId."
                )
            }
        }
    }

    @Test
    fun importsKnownAmbiguousMelodyCaseWithoutChoosingOne() {
        val melodies = readRows("Melody.csv")
            .map(mapper::toMelody)

        val candidates = melodies.filter {
            it.qoloId == 116L &&
                    it.qintoId == 1L
        }

        assertEquals(2, candidates.size)

        assertEquals(
            setOf(119L, 1965L),
            candidates.map { it.id }.toSet()
        )
    }

    @Test
    fun preservesUndeterminedQintoAsSourceFact() {
        val qintos = readRows("Qinto.csv")
            .map(mapper::toQinto)

        assertTrue(
            qintos.any { it.id == 0L }
        )

        val items = readRows("ExistsIn.csv")
            .map(mapper::toExistsIn)

        assertTrue(
            items.any { it.qintoId == 0L }
        )
    }

    @Test
    fun representativeQolosAndMelodiesUseExportedQoloIds() {
        val qolos = readRows("Qolos.csv")
            .map(mapper::toQolo)

        val qoloIds = qolos
            .map { it.id }
            .toSet()

        val melodies = readRows("Melody.csv")
            .map(mapper::toMelody)

        assertTrue(qolos.isNotEmpty())
        assertTrue(melodies.isNotEmpty())

        melodies.forEach { melody ->
            val qoloId = assertNotNull(
                melody.qoloId
            )

            assertTrue(
                qoloId in qoloIds,
                "Melody ${melody.id} references " +
                        "Qolo $qoloId outside the exported Qolo slice."
            )
        }
    }

    private fun readRows(
        fileName: String
    ): List<CsvRow> {
        return reader.read(
            representativeExportPath(fileName)
        ).rows
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