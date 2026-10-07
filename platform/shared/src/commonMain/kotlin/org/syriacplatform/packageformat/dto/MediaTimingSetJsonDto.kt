package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class MediaTimingSetJsonDto(
    val id: Long,
    val mediaAssetId: Long,
    val name: String? = null
)
