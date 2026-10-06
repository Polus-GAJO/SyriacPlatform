package org.syriacplatform.buildtools.source.models

data class ExistsInMediaSource(
    val id: Long,
    val existsInId: Long,
    val mediaAssetId: Long,
    val role: String,
    val sort: Long,
    val mediaTimingSetId: Long?
)
