package org.syriacplatform.content.runtime

import org.syriacplatform.content.models.MediaSegment
import org.syriacplatform.content.models.MediaTimingSet
import org.syriacplatform.content.models.PerformanceMedia
import org.syriacplatform.content.models.TextOccurrenceMediaSegment

/**
 * Timing relation for one TextOccurrence within the timing set
 * referenced by one specific PERFORMANCE recording.
 */
data class ResolvedPerformanceTextOccurrenceTiming(
    val performance: PerformanceMedia,
    val timingSet: MediaTimingSet,
    val relation: TextOccurrenceMediaSegment,
    val segment: MediaSegment
)
