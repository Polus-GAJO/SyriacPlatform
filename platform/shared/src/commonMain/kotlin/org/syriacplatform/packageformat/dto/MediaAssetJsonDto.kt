package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class MediaAssetJsonDto(
    val id: Long,
    val type: String,
    val path: String
)