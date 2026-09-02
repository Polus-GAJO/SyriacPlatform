package org.syriacplatform.audio.queue

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.syriacplatform.audio.contracts.AudioService
import org.syriacplatform.audio.models.PlaybackState
import org.syriacplatform.audio.models.PlaybackStatus
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError

/**
 * Platform-neutral orchestration for an ordered prayer playback queue.
 *
 * This controller does not resolve Qolos, Melodies, or recordings.
 * It receives already-resolved queue entries and delegates playback
 * of one MediaAsset at a time to the existing AudioService.
 */
class PlaybackQueueController(
    private val audioService: AudioService
) {

    private val _state =
        MutableStateFlow(PlaybackQueueState())

    val state: StateFlow<PlaybackQueueState> =
        _state.asStateFlow()

    private var pendingAutoPlay = false

    fun start(
        entries: List<PlaybackQueueEntry>
    ): Result<Unit> {
        if (entries.isEmpty()) {
            return invalidArgument(
                "Playback queue requires at least one entry."
            )
        }

        pendingAutoPlay = false

        when (val stopResult = audioService.stop()) {
            is Result.Success -> Unit
            is Result.Failure -> {
                _state.value =
                    PlaybackQueueState(
                        status = PlaybackQueueStatus.Error,
                        entries = entries
                    )

                return stopResult
            }
        }

        _state.value =
            PlaybackQueueState(
                status = PlaybackQueueStatus.Loading,
                entries = entries,
                currentIndex = 0
            )

        return loadCurrentEntry()
    }

    fun pause(): Result<Unit> =
        audioService.pause()

    fun resume(): Result<Unit> =
        audioService.play()

    fun stop(): Result<Unit> {
        pendingAutoPlay = false

        return when (
            val result = audioService.stop()
        ) {
            is Result.Success -> {
                _state.value =
                    PlaybackQueueState()

                Result.Success(Unit)
            }

            is Result.Failure -> {
                _state.value =
                    _state.value.copy(
                        status =
                            PlaybackQueueStatus.Error
                    )

                result
            }
        }
    }

    /**
     * Feed the canonical AudioService state into queue orchestration.
     *
     * The UI/service integration layer can call this whenever the
     * AudioService StateFlow changes. The controller advances only when
     * the state belongs to the queue's current MediaAsset.
     */
    fun handlePlaybackState(
        playbackState: PlaybackState
    ) {
        val current =
            _state.value.currentEntry
                ?: return

        if (
            playbackState.mediaAssetId !=
            current.mediaAsset.id
        ) {
            return
        }

        when (playbackState.status) {
            PlaybackStatus.Ready -> {
                if (pendingAutoPlay) {
                    when (
                        audioService.play()
                    ) {
                        is Result.Success -> {
                            pendingAutoPlay = false
                        }

                        is Result.Failure -> {
                            pendingAutoPlay = false

                            _state.value =
                                _state.value.copy(
                                    status =
                                        PlaybackQueueStatus.Error
                                )
                        }
                    }
                }
            }

            PlaybackStatus.Playing -> {
                _state.value =
                    _state.value.copy(
                        status =
                            PlaybackQueueStatus.Playing
                    )
            }

            PlaybackStatus.Paused -> {
                _state.value =
                    _state.value.copy(
                        status =
                            PlaybackQueueStatus.Paused
                    )
            }

            PlaybackStatus.Ended -> {
                advanceAfterEnded()
            }

            PlaybackStatus.Error -> {
                pendingAutoPlay = false

                _state.value =
                    _state.value.copy(
                        status =
                            PlaybackQueueStatus.Error
                    )
            }

            PlaybackStatus.Loading -> {
                _state.value =
                    _state.value.copy(
                        status =
                            PlaybackQueueStatus.Loading
                    )
            }

            PlaybackStatus.Idle -> Unit
        }
    }

    private fun advanceAfterEnded() {
        val currentState =
            _state.value

        val index =
            currentState.currentIndex
                ?: return

        val nextIndex =
            index + 1

        if (
            nextIndex >=
            currentState.entries.size
        ) {
            pendingAutoPlay = false

            _state.value =
                currentState.copy(
                    status =
                        PlaybackQueueStatus.Completed
                )

            return
        }

        _state.value =
            currentState.copy(
                status =
                    PlaybackQueueStatus.Loading,
                currentIndex =
                    nextIndex
            )

        loadCurrentEntry()
    }

    private fun loadCurrentEntry():
        Result<Unit> {
        val current =
            _state.value.currentEntry
                ?: return invalidState(
                    "Playback queue has no current entry."
                )

        pendingAutoPlay = true

        return when (
            val result =
                audioService.load(
                    current.mediaAsset
                )
        ) {
            is Result.Success ->
                Result.Success(Unit)

            is Result.Failure -> {
                pendingAutoPlay = false

                _state.value =
                    _state.value.copy(
                        status =
                            PlaybackQueueStatus.Error
                    )

                result
            }
        }
    }

    private fun invalidArgument(
        message: String
    ): Result.Failure =
        Result.Failure(
            PlatformError(
                code = ErrorCode.INVALID_ARGUMENT,
                message = message
            )
        )

    private fun invalidState(
        message: String
    ): Result.Failure =
        Result.Failure(
            PlatformError(
                code = ErrorCode.INVALID_STATE,
                message = message
            )
        )
}
