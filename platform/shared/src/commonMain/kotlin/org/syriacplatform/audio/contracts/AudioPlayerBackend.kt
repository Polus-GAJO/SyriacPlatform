package org.syriacplatform.audio.contracts

import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.common.result.Result

/**
 * Boundary between Core audio intent and a real playback engine.
 *
 * Implementations may wrap Android, iOS, desktop, or test players.
 * The Core service owns validation and canonical PlaybackState;
 * the backend executes the requested playback command.
 */
interface AudioPlayerBackend {

    fun prepare(
        resource: MediaResource
    ): Result<Unit>

    fun play(): Result<Unit>

    fun pause(): Result<Unit>

    fun stop(): Result<Unit>

    fun seekTo(
        positionMs: Long
    ): Result<Unit>
}
