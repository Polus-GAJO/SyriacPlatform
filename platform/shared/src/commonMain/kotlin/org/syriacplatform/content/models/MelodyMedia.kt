package org.syriacplatform.content.models

import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId

/** Published relationship between a melody and a packaged media asset. */
data class MelodyMedia(
    val id: Long,
    val melodyId: MelodyId,
    val mediaAssetId: MediaAssetId,
    val role: String,
    val sort: Int
)
