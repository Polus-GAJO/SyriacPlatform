package org.syriacplatform.content.models

import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MediaTimingSetId

data class MediaTimingSet(
    val id: MediaTimingSetId,
    val mediaAssetId: MediaAssetId,
    val name: String? = null
)
