package org.syriacplatform.packagevalidation

/**
 * النتيجة الكاملة لعملية التحقق.
 *
 * يجمع التقرير جميع المشكلات بدل التوقف عند أول مشكلة.
 */
data class ValidationReport(
    val issues: List<ValidationIssue>
) {

    val isValid: Boolean
        get() = issues.none { issue ->
            issue.severity == ValidationSeverity.FATAL
        }

    val fatalIssues: List<ValidationIssue>
        get() = issues.filter { issue ->
            issue.severity == ValidationSeverity.FATAL
        }

    val recoverableIssues: List<ValidationIssue>
        get() = issues.filter { issue ->
            issue.severity == ValidationSeverity.RECOVERABLE
        }

    val warnings: List<ValidationIssue>
        get() = issues.filter { issue ->
            issue.severity == ValidationSeverity.WARNING
        }

    val information: List<ValidationIssue>
        get() = issues.filter { issue ->
            issue.severity == ValidationSeverity.INFORMATION
        }

    companion object {

        fun valid(): ValidationReport {
            return ValidationReport(
                issues = emptyList()
            )
        }
    }
}