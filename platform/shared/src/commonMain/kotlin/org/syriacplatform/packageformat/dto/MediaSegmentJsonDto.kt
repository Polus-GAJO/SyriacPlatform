package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class MediaSegmentJsonDto(
    val id: Long,
    val mediaTimingSetId: Long,
    val sequence: Long,
    val startMs: Long? = null,
    val endMs: Long? = null
)
