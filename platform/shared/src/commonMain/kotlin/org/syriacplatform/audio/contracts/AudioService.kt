package org.syriacplatform.audio.contracts

import kotlinx.coroutines.flow.StateFlow
import org.syriacplatform.audio.models.PlaybackState
import org.syriacplatform.common.result.Result
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.kernel.PlatformService

/**
 * Platform-neutral audio service contract.
 *
 * This contract owns playback intent and observable state.
 * Native playback engines implement it later on each platform.
 */
interface AudioService : PlatformService {

    val state: StateFlow<PlaybackState>

    fun load(
        mediaAsset: MediaAsset
    ): Result<Unit>

    fun play(): Result<Unit>

    fun pause(): Result<Unit>

    fun stop(): Result<Unit>

    fun seekTo(
        positionMs: Long
    ): Result<Unit>
}
