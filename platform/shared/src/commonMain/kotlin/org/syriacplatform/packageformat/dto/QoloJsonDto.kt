package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

@Serializable
data class QoloJsonDto(
    val id: Long,
    val groupId: Long,
    val sort: Long,
    val name: String,
    val searchName: String,
    val poeticMeter: String? = null
)