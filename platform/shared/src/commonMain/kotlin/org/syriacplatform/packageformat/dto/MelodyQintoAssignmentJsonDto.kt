package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class MelodyQintoAssignmentJsonDto(
    val melodyId: Long,
    val qintoId: Long,
    val role: String? = null
)