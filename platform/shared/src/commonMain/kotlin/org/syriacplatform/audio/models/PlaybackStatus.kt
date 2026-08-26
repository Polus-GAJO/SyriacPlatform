package org.syriacplatform.audio.models

/**
 * Platform-neutral playback lifecycle.
 *
 * This deliberately describes playback semantics only.
 * Native player state remains an implementation detail.
 */
enum class PlaybackStatus {
    Idle,
    Loading,
    Ready,
    Playing,
    Paused,
    Ended,
    Error
}
