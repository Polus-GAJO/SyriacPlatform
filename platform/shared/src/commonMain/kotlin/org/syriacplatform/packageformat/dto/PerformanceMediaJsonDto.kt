package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PerformanceMediaJsonDto(
    val id: Long,
    val liturgicalItemId: Long,
    val mediaAssetId: Long,
    val role: String,
    val sort: Long,
    val mediaTimingSetId: Long? = null
)
