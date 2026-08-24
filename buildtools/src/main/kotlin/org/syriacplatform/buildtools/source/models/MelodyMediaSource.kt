package org.syriacplatform.buildtools.source.models

data class MelodyMediaSource(
    val id: Long,
    val melodyId: Long,
    val mediaAssetId: Long,
    val role: String,
    val sort: Long
)