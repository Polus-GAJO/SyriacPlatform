package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * سجل فتغام قانوني مستقل داخل petgomos.json.
 */
@Serializable
internal data class PetgomoJsonDto(
    val id: Long,
    val syriac: String,
    val translations: List<TextTranslationJsonDto> = emptyList()
)