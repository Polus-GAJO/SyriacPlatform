package org.syriacplatform.packageformat.models

/**
 * معلومات عملية البناء التي أنتجت الحزمة.
 */
data class BuildInfo(
    val generatedAt: String,
    val buildTool: String,
    val buildVersion: String,
    val buildRevision: String
)