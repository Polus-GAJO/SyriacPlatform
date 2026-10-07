package org.syriacplatform.content.runtime

import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.MediaSegment
import org.syriacplatform.content.models.PerformanceMedia

/**
 * A complete, playable interval for one TextOccurrence
 * within one specific PERFORMANCE recording.
 */
data class ResolvedPerformanceTextOccurrenceInterval(
    val performance: PerformanceMedia,
    val mediaAsset: MediaAsset,
    val segment: MediaSegment,
    val startMs: Long,
    val endMs: Long
)
