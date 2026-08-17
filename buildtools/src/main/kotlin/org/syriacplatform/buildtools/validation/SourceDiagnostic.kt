package org.syriacplatform.buildtools.validation

data class SourceDiagnostic(
    val severity: DiagnosticSeverity,
    val code: String,
    val message: String,
    val sourceType: String? = null,
    val sourceId: Long? = null
)