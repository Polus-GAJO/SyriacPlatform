package org.syriacplatform.audio.models

import org.syriacplatform.common.types.MediaAssetId

/**
 * Observable, platform-neutral audio playback state.
 *
 * Time values are expressed in milliseconds so common code
 * does not depend on a platform-specific duration type.
 */
data class PlaybackState(
    val status: PlaybackStatus = PlaybackStatus.Idle,
    val mediaAssetId: MediaAssetId? = null,
    val positionMs: Long = 0L,
    val durationMs: Long? = null
)
