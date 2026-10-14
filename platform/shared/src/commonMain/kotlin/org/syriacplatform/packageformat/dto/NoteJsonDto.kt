package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class NoteJsonDto(
    val id: Long,
    val text: String? = null
)