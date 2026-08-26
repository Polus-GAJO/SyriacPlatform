package org.syriacplatform.audio.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.syriacplatform.audio.contracts.AudioService
import org.syriacplatform.audio.contracts.MediaResourceResolver
import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.audio.models.PlaybackState
import org.syriacplatform.audio.models.PlaybackStatus
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.kernel.ServiceMetadata

/**
 * Platform-neutral AudioService state machine.
 *
 * This implementation validates playback intent and owns observable
 * playback state, but deliberately does not invoke a native player.
 * Native playback integration will be added behind a separate boundary.
 */
class DefaultAudioService(
    private val resourceResolver: MediaResourceResolver
) : AudioService {

    override val metadata =
        ServiceMetadata(
            name = "Audio Service",
            version = "1.0"
        )

    override var runtimeState =
        RuntimeState.NotInitialized
        private set

    private val _state =
        MutableStateFlow(
            PlaybackState()
        )

    override val state: StateFlow<PlaybackState> =
        _state.asStateFlow()

    private var currentResource: MediaResource? =
        null

    override fun initialize() {
        runtimeState =
            RuntimeState.Ready
    }

    override fun load(
        mediaAsset: MediaAsset
    ): Result<Unit> {
        val readiness =
            requireReadyService()

        if (readiness is Result.Failure) {
            return readiness
        }

        return when (
            val resolution =
                resourceResolver.resolve(
                    mediaAsset
                )
        ) {
            is Result.Success -> {
                currentResource =
                    resolution.data

                _state.value =
                    PlaybackState(
                        status =
                            PlaybackStatus.Ready,
                        mediaAssetId =
                            mediaAsset.id,
                        positionMs = 0L,
                        durationMs = null
                    )

                Result.Success(Unit)
            }

            is Result.Failure -> {
                currentResource = null

                _state.value =
                    PlaybackState(
                        status =
                            PlaybackStatus.Error,
                        mediaAssetId =
                            mediaAsset.id,
                        positionMs = 0L,
                        durationMs = null
                    )

                resolution
            }
        }
    }

    override fun play(): Result<Unit> {
        val readiness =
            requireReadyService()

        if (readiness is Result.Failure) {
            return readiness
        }

        if (currentResource == null) {
            return invalidState(
                "Audio play requires a loaded media resource."
            )
        }

        val current =
            _state.value

        if (
            current.status != PlaybackStatus.Ready &&
            current.status != PlaybackStatus.Paused
        ) {
            return invalidState(
                "Audio cannot play from state ${current.status}."
            )
        }

        _state.value =
            current.copy(
                status =
                    PlaybackStatus.Playing
            )

        return Result.Success(Unit)
    }

    override fun pause(): Result<Unit> {
        val readiness =
            requireReadyService()

        if (readiness is Result.Failure) {
            return readiness
        }

        val current =
            _state.value

        if (
            current.status !=
            PlaybackStatus.Playing
        ) {
            return invalidState(
                "Audio can pause only while playing."
            )
        }

        _state.value =
            current.copy(
                status =
                    PlaybackStatus.Paused
            )

        return Result.Success(Unit)
    }

    override fun stop(): Result<Unit> {
        val readiness =
            requireReadyService()

        if (readiness is Result.Failure) {
            return readiness
        }

        currentResource = null
        _state.value =
            PlaybackState()

        return Result.Success(Unit)
    }

    override fun seekTo(
        positionMs: Long
    ): Result<Unit> {
        val readiness =
            requireReadyService()

        if (readiness is Result.Failure) {
            return readiness
        }

        if (positionMs < 0L) {
            return Result.Failure(
                PlatformError(
                    code =
                        ErrorCode.INVALID_ARGUMENT,
                    message =
                        "Audio seek position cannot be negative."
                )
            )
        }

        val current =
            _state.value

        if (
            current.status != PlaybackStatus.Ready &&
            current.status != PlaybackStatus.Playing &&
            current.status != PlaybackStatus.Paused
        ) {
            return invalidState(
                "Audio seek requires loaded media."
            )
        }

        _state.value =
            current.copy(
                positionMs =
                    positionMs
            )

        return Result.Success(Unit)
    }

    private fun requireReadyService(): Result<Unit> {
        if (
            runtimeState !=
            RuntimeState.Ready
        ) {
            return invalidState(
                "Audio Service is not initialized."
            )
        }

        return Result.Success(Unit)
    }

    private fun invalidState(
        message: String
    ): Result.Failure {
        return Result.Failure(
            PlatformError(
                code =
                    ErrorCode.INVALID_STATE,
                message =
                    message
            )
        )
    }
}
