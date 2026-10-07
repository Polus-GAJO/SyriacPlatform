package org.syriacplatform.content.runtime

import org.syriacplatform.content.models.MediaSegment
import org.syriacplatform.content.models.MediaTimingSet
import org.syriacplatform.content.models.PerformanceMedia

/**
 * PERFORMANCE timing after resolving its referenced MediaTimingSet
 * and the segments that belong to that timing set.
 */
data class ResolvedPerformanceTiming(
    val performance: PerformanceMedia,
    val timingSet: MediaTimingSet,
    val segments: List<MediaSegment>
)
