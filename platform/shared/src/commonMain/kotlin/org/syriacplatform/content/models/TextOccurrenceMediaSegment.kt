package org.syriacplatform.content.models

import org.syriacplatform.common.types.MediaSegmentId
import org.syriacplatform.common.types.TextOccurrenceId
import org.syriacplatform.common.types.TextOccurrenceMediaSegmentId

data class TextOccurrenceMediaSegment(
    val id: TextOccurrenceMediaSegmentId,
    val textOccurrenceId: TextOccurrenceId,
    val mediaSegmentId: MediaSegmentId
)
