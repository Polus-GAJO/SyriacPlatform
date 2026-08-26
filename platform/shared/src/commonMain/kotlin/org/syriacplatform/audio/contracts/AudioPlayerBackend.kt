package org.syriacplatform.audio.contracts

import org.syriacplatform.audio.models.AudioPlayerEvent
import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.common.result.Result

interface AudioPlayerBackend {
    fun setEventListener(
        listener: ((AudioPlayerEvent) -> Unit)?
    )

    fun prepare(resource: MediaResource): Result<Unit>
    fun play(): Result<Unit>
    fun pause(): Result<Unit>
    fun stop(): Result<Unit>
    fun seekTo(positionMs: Long): Result<Unit>
}
