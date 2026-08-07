package org.syriacplatform.packagevalidation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.syriacplatform.common.types.ErrorCode

class ValidationReportTest {

    @Test
    fun emptyReportIsValid() {
        val report = ValidationReport.valid()

        assertTrue(report.isValid)
        assertTrue(report.issues.isEmpty())
    }

    @Test
    fun fatalIssueMakesReportInvalid() {
        val report = ValidationReport(
            issues = listOf(
                ValidationIssue(
                    severity = ValidationSeverity.FATAL,
                    code = ErrorCode.INVALID_PACKAGE_DATA,
                    message = "Package data is invalid."
                )
            )
        )

        assertFalse(report.isValid)
        assertEquals(1, report.fatalIssues.size)
    }

    @Test
    fun warningDoesNotInvalidatePackage() {
        val report = ValidationReport(
            issues = listOf(
                ValidationIssue(
                    severity = ValidationSeverity.WARNING,
                    code = ErrorCode.INVALID_PACKAGE_DATA,
                    message = "Optional metadata is missing."
                )
            )
        )

        assertTrue(report.isValid)
        assertEquals(1, report.warnings.size)
    }

    @Test
    fun issuesAreGroupedBySeverity() {
        val report = ValidationReport(
            issues = listOf(
                ValidationIssue(
                    ValidationSeverity.FATAL,
                    ErrorCode.INVALID_REFERENCE,
                    "Required reference is missing."
                ),
                ValidationIssue(
                    ValidationSeverity.RECOVERABLE,
                    ErrorCode.RESOURCE_UNAVAILABLE,
                    "Optional audio is unavailable."
                ),
                ValidationIssue(
                    ValidationSeverity.WARNING,
                    ErrorCode.INVALID_PACKAGE_DATA,
                    "Recommended metadata is missing."
                ),
                ValidationIssue(
                    ValidationSeverity.INFORMATION,
                    ErrorCode.INVALID_PACKAGE_DATA,
                    "Several items share the same sort value."
                )
            )
        )

        assertEquals(1, report.fatalIssues.size)
        assertEquals(1, report.recoverableIssues.size)
        assertEquals(1, report.warnings.size)
        assertEquals(1, report.information.size)
    }
}