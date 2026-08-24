package org.syriacplatform.buildtools.source.models

data class MediaAssetSource(
    val id: Long,
    val mediaType: String,
    val sourceRelativePath: String
)