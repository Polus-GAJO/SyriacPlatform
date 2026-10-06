package org.syriacplatform.buildtools.source.models

data class MediaSegmentSource(
    val id: Long,
    val mediaTimingSetId: Long,
    val sequence: Long,
    val startMs: Long?,
    val endMs: Long?
)
