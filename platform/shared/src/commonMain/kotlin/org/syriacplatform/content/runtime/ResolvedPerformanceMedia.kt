package org.syriacplatform.content.runtime

import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.PerformanceMedia

/**
 * PERFORMANCE relation after resolving its canonical MediaAsset.
 *
 * Timing remains unresolved at this layer; it is resolved separately.
 */
data class ResolvedPerformanceMedia(
    val performance: PerformanceMedia,
    val mediaAsset: MediaAsset
)
