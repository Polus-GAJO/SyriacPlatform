package org.syriacplatform.audio.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.syriacplatform.audio.contracts.AudioPlayerBackend
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
 * MediaResourceResolver converts canonical package metadata into a
 * playable resource reference. AudioPlayerBackend executes the actual
 * player commands. This service owns command validation and the
 * canonical PlaybackState exposed to the rest of Core.
 */
class DefaultAudioService(
    private val resourceResolver: MediaResourceResolver,
    private val playerBackend: AudioPlayerBackend
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

        val resource =
            when (
                val resolution =
                    resourceResolver.resolve(
                        mediaAsset
                    )
            ) {
                is Result.Success ->
                    resolution.data

                is Result.Failure -> {
                    currentResource = null

                    _state.value =
                        PlaybackState(
                            status =
                                PlaybackStatus.Error,
                            mediaAssetId =
                                mediaAsset.id
                        )

                    return resolution
                }
            }

        return when (
            val preparation =
                playerBackend.prepare(
                    resource
                )
        ) {
            is Result.Success -> {
                currentResource =
                    resource

                _state.value =
                    PlaybackState(
                        status =
                            PlaybackStatus.Ready,
                        mediaAssetId =
                            mediaAsset.id
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
                            mediaAsset.id
                    )

                preparation
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

        return when (
            val result =
                playerBackend.play()
        ) {
            is Result.Success -> {
                _state.value =
                    current.copy(
                        status =
                            PlaybackStatus.Playing
                    )

                Result.Success(Unit)
            }

            is Result.Failure ->
                failPlayback(
                    result
                )
        }
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

        return when (
            val result =
                playerBackend.pause()
        ) {
            is Result.Success -> {
                _state.value =
                    current.copy(
                        status =
                            PlaybackStatus.Paused
                    )

                Result.Success(Unit)
            }

            is Result.Failure ->
                failPlayback(
                    result
                )
        }
    }

    override fun stop(): Result<Unit> {
        val readiness =
            requireReadyService()

        if (readiness is Result.Failure) {
            return readiness
        }

        return when (
            val result =
                playerBackend.stop()
        ) {
            is Result.Success -> {
                currentResource = null

                _state.value =
                    PlaybackState()

                Result.Success(Unit)
            }

            is Result.Failure ->
                failPlayback(
                    result
                )
        }
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

        return when (
            val result =
                playerBackend.seekTo(
                    positionMs
                )
        ) {
            is Result.Success -> {
                _state.value =
                    current.copy(
                        positionMs =
                            positionMs
                    )

                Result.Success(Unit)
            }

            is Result.Failure ->
                failPlayback(
                    result
                )
        }
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

    private fun failPlayback(
        failure: Result.Failure
    ): Result.Failure {
        _state.value =
            _state.value.copy(
                status =
                    PlaybackStatus.Error
            )

        return failure
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
