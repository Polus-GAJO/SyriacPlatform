package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * سجل بيت شعري واحد داخل texts.json.
 */
@Serializable
internal data class TextContentJsonDto(
    val id: Long,
    val syriac: String,
    val translations: List<TextTranslationJsonDto> = emptyList()
)