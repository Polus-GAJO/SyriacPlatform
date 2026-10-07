package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * البنية الفيزيائية لظهور نص سياقي داخل
 * ظهور ترتيلة Qolo في LiturgicalItem.
 *
 * id هو هوية TextOccurrence السياقية المستقلة،
 * بينما textId يشير إلى TextContent القابل لإعادة الاستخدام.
 */
@Serializable
internal data class TextOccurrenceJsonDto(
    val id: Long,
    val textId: Long,
    val petgomoId: Long? = null
)

internal typealias LiturgicalTextRefJsonDto =
    TextOccurrenceJsonDto
