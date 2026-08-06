package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * التمثيل الفيزيائي لترجمة واحدة داخل JSON.
 */
@Serializable
internal data class TextTranslationJsonDto(
    val language: String,
    val content: String
)