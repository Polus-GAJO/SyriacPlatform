package org.syriacplatform.buildtools.validation

data class SourceValidationReport(
    val diagnostics: List<SourceDiagnostic>
) {
    val errors: List<SourceDiagnostic>
        get() = diagnostics.filter {
            it.severity == DiagnosticSeverity.ERROR
        }

    val warnings: List<SourceDiagnostic>
        get() = diagnostics.filter {
            it.severity == DiagnosticSeverity.WARNING
        }

    val infos: List<SourceDiagnostic>
        get() = diagnostics.filter {
            it.severity == DiagnosticSeverity.INFO
        }

    val hasErrors: Boolean
        get() = errors.isNotEmpty()

    val canGeneratePackage: Boolean
        get() = !hasErrors
}