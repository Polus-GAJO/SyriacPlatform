package org.syriacplatform.audio.queue

import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.content.models.MediaAsset

/**
 * One already-resolved playable entry in a prayer playback queue.
 *
 * Content/runtime selection happens before an entry reaches the queue:
 * a contextual Qolo contributes one or more valid Melodies, and each
 * queued Melody contributes exactly one selected recording.
 */
data class PlaybackQueueEntry(
    val liturgicalItemId: LiturgicalItemId,
    val melodyId: MelodyId,
    val mediaAsset: MediaAsset
)
