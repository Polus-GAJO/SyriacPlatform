package org.syriacplatform.packagevalidation

import org.syriacplatform.common.types.ErrorCode

/**
 * مشكلة واحدة اكتُشفت أثناء التحقق من الحزمة.
 */
data class ValidationIssue(
    val severity: ValidationSeverity,
    val code: ErrorCode,
    val message: String,
    val location: String? = null
)