package org.syriacplatform.audio.android

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.syriacplatform.audio.contracts.AudioPlayerBackend
import org.syriacplatform.audio.models.AudioPlayerEvent
import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError

class AndroidAudioPlayerBackend(
    context: Context
) : AudioPlayerBackend {

    private val player =
        ExoPlayer.Builder(
            context.applicationContext
        ).build()

    private var eventListener:
        ((AudioPlayerEvent) -> Unit)? =
        null

    private var awaitingInitialReady =
        false

    private var pausePending =
        false

    private val positionHandler =
        Handler(
            Looper.getMainLooper()
        )

    private var positionUpdatesActive =
        false

    private val positionUpdateRunnable =
        object : Runnable {
            override fun run() {
                if (
                    !positionUpdatesActive ||
                    !player.isPlaying
                ) {
                    return
                }

                emitCurrentPosition()

                positionHandler.postDelayed(
                    this,
                    POSITION_UPDATE_INTERVAL_MS
                )
            }
        }

    init {
        player.addListener(
            object : Player.Listener {

                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            if (awaitingInitialReady) {
                                awaitingInitialReady =
                                    false

                                eventListener?.invoke(
                                    AudioPlayerEvent.Ready(
                                        durationMs =
                                            normalizedDuration()
                                    )
                                )
                            }
                        }

                        Player.STATE_ENDED -> {
                            pausePending =
                                false

                            stopPositionUpdates()
                            emitCurrentPosition()

                            eventListener?.invoke(
                                AudioPlayerEvent.Ended
                            )
                        }
                    }
                }

                override fun onIsPlayingChanged(
                    isPlaying: Boolean
                ) {
                    if (isPlaying) {
                        pausePending =
                            false

                        eventListener?.invoke(
                            AudioPlayerEvent.Playing
                        )

                        startPositionUpdates()

                        return
                    }

                    stopPositionUpdates()

                    if (
                        pausePending &&
                        player.playbackState ==
                        Player.STATE_READY
                    ) {
                        pausePending =
                            false

                        emitCurrentPosition()

                        eventListener?.invoke(
                            AudioPlayerEvent.Paused
                        )
                    }
                }

                override fun onPlayerError(
                    error: PlaybackException
                ) {
                    awaitingInitialReady =
                        false

                    pausePending =
                        false

                    stopPositionUpdates()

                    eventListener?.invoke(
                        AudioPlayerEvent.Error(
                            PlatformError(
                                code =
                                    ErrorCode.AUDIO_ERROR,
                                message =
                                    "Android audio playback failed: " +
                                            error.errorCodeName,
                                cause =
                                    error
                            )
                        )
                    )
                }
            }
        )
    }

    override fun setEventListener(
        listener: ((AudioPlayerEvent) -> Unit)?
    ) {
        eventListener =
            listener
    }

    override fun prepare(
        resource: MediaResource
    ): Result<Unit> {
        return execute(
            operation = "prepare"
        ) {
            pausePending =
                false

            awaitingInitialReady =
                true

            stopPositionUpdates()

            player.stop()
            player.clearMediaItems()

            player.setMediaItem(
                MediaItem.fromUri(
                    resource.uri
                )
            )

            player.prepare()
        }
    }

    override fun play(): Result<Unit> {
        return execute(
            operation = "play"
        ) {
            pausePending =
                false

            player.play()
        }
    }

    override fun pause(): Result<Unit> {
        return execute(
            operation = "pause",
            onFailure = {
                pausePending =
                    false
            }
        ) {
            pausePending =
                true

            player.pause()
        }
    }

    override fun stop(): Result<Unit> {
        return execute(
            operation = "stop"
        ) {
            awaitingInitialReady =
                false

            pausePending =
                false

            stopPositionUpdates()

            player.stop()
            player.clearMediaItems()
        }
    }

    override fun seekTo(
        positionMs: Long
    ): Result<Unit> {
        return execute(
            operation = "seek"
        ) {
            player.seekTo(
                positionMs
            )

            emitCurrentPosition()
        }
    }

    override fun release() {
        stopPositionUpdates()

        eventListener =
            null

        player.release()
    }

    private fun startPositionUpdates() {
        if (positionUpdatesActive) {
            return
        }

        positionUpdatesActive =
            true

        emitCurrentPosition()

        positionHandler.postDelayed(
            positionUpdateRunnable,
            POSITION_UPDATE_INTERVAL_MS
        )
    }

    private fun stopPositionUpdates() {
        positionUpdatesActive =
            false

        positionHandler.removeCallbacks(
            positionUpdateRunnable
        )
    }

    private fun emitCurrentPosition() {
        val positionMs =
            player.currentPosition
                .coerceAtLeast(0L)

        eventListener?.invoke(
            AudioPlayerEvent.PositionChanged(
                positionMs =
                    positionMs
            )
        )
    }

    private fun normalizedDuration(): Long? {
        val duration =
            player.duration

        return duration.takeIf {
            it != C.TIME_UNSET &&
                    it >= 0L
        }
    }

    private fun execute(
        operation: String,
        onFailure: () -> Unit = {},
        block: () -> Unit
    ): Result<Unit> {
        return try {
            block()

            Result.Success(
                Unit
            )
        } catch (
            error: Throwable
        ) {
            onFailure()

            Result.Failure(
                PlatformError(
                    code =
                        ErrorCode.AUDIO_ERROR,
                    message =
                        "Android audio backend failed to " +
                                "$operation.",
                    cause =
                        error
                )
            )
        }
    }

    private companion object {
        const val POSITION_UPDATE_INTERVAL_MS =
            250L
    }
}
