package org.syriacplatform.content.runtime

import org.syriacplatform.content.models.MediaSegment
import org.syriacplatform.content.models.MediaTimingSet
import org.syriacplatform.content.models.TextOccurrenceMediaSegment

/**
 * Timing relation for one contextual TextOccurrence after resolving
 * the referenced MediaSegment and its MediaTimingSet.
 *
 * A TextOccurrence may resolve to zero or more timing relations.
 */
data class ResolvedTextOccurrenceTiming(
    val relation: TextOccurrenceMediaSegment,
    val segment: MediaSegment,
    val timingSet: MediaTimingSet
)
