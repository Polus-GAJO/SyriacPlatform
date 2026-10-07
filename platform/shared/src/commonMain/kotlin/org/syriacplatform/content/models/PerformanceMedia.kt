package org.syriacplatform.content.models

import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MediaTimingSetId
import org.syriacplatform.common.types.PerformanceMediaId

data class PerformanceMedia(
    val id: PerformanceMediaId,
    val liturgicalItemId: LiturgicalItemId,
    val mediaAssetId: MediaAssetId,
    val role: String,
    val sort: Long,
    val mediaTimingSetId: MediaTimingSetId? = null
)
