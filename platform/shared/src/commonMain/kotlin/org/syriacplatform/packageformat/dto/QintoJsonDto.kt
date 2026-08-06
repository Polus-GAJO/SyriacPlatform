package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class QintoJsonDto(
    val id: Long,
    val name: String
)