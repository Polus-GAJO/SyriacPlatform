package org.syriacplatform.audio.models

import org.syriacplatform.common.types.PlatformError

sealed interface AudioPlayerEvent {
    data class Ready(
        val durationMs: Long?
    ) : AudioPlayerEvent

    data object Playing : AudioPlayerEvent
    data object Paused : AudioPlayerEvent
    data object Ended : AudioPlayerEvent

    data class PositionChanged(
        val positionMs: Long
    ) : AudioPlayerEvent

    data class Error(
        val error: PlatformError
    ) : AudioPlayerEvent
}
