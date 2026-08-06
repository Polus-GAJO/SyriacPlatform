package org.syriacplatform.packageformat.models

/**
 * متطلبات التوافق اللازمة لقراءة الحزمة.
 */
data class CompatibilityInfo(
    val minimumCoreVersion: String,
    val targetSchemaVersion: String,
    val supportedFeatures: List<String>
)