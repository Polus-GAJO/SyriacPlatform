package org.syriacplatform.packageformat.models

/**
 * معلومات التطبيق المستهدف بالحزمة.
 */
data class ApplicationInfo(
    val id: String,
    val name: String,
    val platform: String,
    val defaultLanguage: String
)