package org.syriacplatform.content.models

import org.syriacplatform.common.types.MediaSegmentId
import org.syriacplatform.common.types.MediaTimingSetId

data class MediaSegment(
    val id: MediaSegmentId,
    val mediaTimingSetId: MediaTimingSetId,
    val sequence: Long,
    val startMs: Long?,
    val endMs: Long?
)
