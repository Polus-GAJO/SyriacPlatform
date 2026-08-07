package org.syriacplatform.packagevalidation

import kotlin.test.Test
import kotlin.test.assertEquals
import org.syriacplatform.common.types.ErrorCode

class PackageValidationRuleTest {

    @Test
    fun ruleReturnsAllDetectedIssues() {
        val rule = PackageValidationRule<String> { value ->
            buildList {
                if (value.isBlank()) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.MISSING_REQUIRED_FIELD,
                            message = "Value is required."
                        )
                    )
                }

                if (value.length < 3) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.WARNING,
                            code = ErrorCode.INVALID_PACKAGE_DATA,
                            message = "Value is shorter than recommended."
                        )
                    )
                }
            }
        }

        val issues = rule.validate("")

        assertEquals(2, issues.size)
        assertEquals(
            ValidationSeverity.FATAL,
            issues[0].severity
        )
        assertEquals(
            ValidationSeverity.WARNING,
            issues[1].severity
        )
    }
}