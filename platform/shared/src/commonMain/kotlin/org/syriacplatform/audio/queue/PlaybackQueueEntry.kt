package org.syriacplatform.audio.queue

import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.content.models.MediaAsset

/** One contextual performance recording in prayer order. */
data class PlaybackQueueEntry(
    val liturgicalItemId: LiturgicalItemId,
    val mediaAsset: MediaAsset
)
