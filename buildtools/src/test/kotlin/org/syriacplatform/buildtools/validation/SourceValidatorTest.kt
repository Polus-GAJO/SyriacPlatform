package org.syriacplatform.buildtools.validation

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader

class SourceValidatorTest {

    private val loader = AuthorSourceDataLoader()
    private val validator = SourceValidator()

    @Test
    fun representativeExportHasNoStructuralErrors() {
        val report = validateRepresentativeExport()

        assertFalse(
            report.hasErrors,
            report.errors.joinToString("\n") {
                "${it.code}: ${it.message}"
            }
        )

        assertTrue(
            report.canGeneratePackage
        )
    }

    @Test
    fun reportsUndeterminedQintosAsWarnings() {
        val report = validateRepresentativeExport()

        val unresolved = report.diagnostics.filter {
            it.code == "UNRESOLVED_QINTO"
        }

        assertEquals(
            13,
            unresolved.size
        )

        assertTrue(
            unresolved.all {
                it.severity ==
                        DiagnosticSeverity.WARNING
            }
        )
    }

    @Test
    fun reportsKnownAmbiguousMelodyOccurrences() {
        val report = validateRepresentativeExport()

        val ambiguous = report.diagnostics.filter {
            it.code == "AMBIGUOUS_MELODY"
        }

        assertEquals(
            3,
            ambiguous.size
        )

        assertTrue(
            ambiguous.all {
                "119" in it.message &&
                        "1965" in it.message
            }
        )
    }

    @Test
    fun incompleteTextAssignmentsAreInformational() {
        val report = validateRepresentativeExport()

        val incomplete = report.diagnostics.filter {
            it.code == "NO_TEXT_OCCURRENCES"
        }

        assertEquals(
            7,
            incomplete.size
        )

        assertTrue(
            incomplete.all {
                it.severity ==
                        DiagnosticSeverity.INFO
            }
        )
    }

    @Test
    fun equalTextSortValuesAreNotReportedAsErrors() {
        val report = validateRepresentativeExport()

        assertTrue(
            report.diagnostics.none {
                it.code.contains(
                    "DUPLICATE_SORT"
                )
            }
        )
    }

    private fun validateRepresentativeExport():
            SourceValidationReport {

        val data = loader.load(
            representativeExportDirectory()
        )

        return validator.validate(data)
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