package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class TextOccurrenceMediaSegmentJsonDto(
    val id: Long,
    val textOccurrenceId: Long,
    val mediaSegmentId: Long
)
