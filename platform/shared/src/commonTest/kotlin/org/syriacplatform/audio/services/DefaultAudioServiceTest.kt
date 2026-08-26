package org.syriacplatform.audio.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.syriacplatform.audio.contracts.AudioPlayerBackend
import org.syriacplatform.audio.contracts.MediaResourceResolver
import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.audio.models.PlaybackStatus
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.models.MediaAsset

class DefaultAudioServiceTest {

    @Test
    fun canonicalStateFlowDelegatesCommandsToBackendInOrder() {
        val asset =
            mediaAsset(
                id = 293L
            )

        val backend =
            RecordingBackend()

        val service =
            service(
                backend = backend
            )

        assertEquals(
            RuntimeState.NotInitialized,
            service.runtimeState
        )

        service.initialize()

        assertIs<Result.Success<Unit>>(
            service.load(asset)
        )
        assertEquals(
            PlaybackStatus.Ready,
            service.state.value.status
        )

        assertIs<Result.Success<Unit>>(
            service.play()
        )

        assertIs<Result.Success<Unit>>(
            service.seekTo(12_345L)
        )

        assertIs<Result.Success<Unit>>(
            service.pause()
        )

        assertIs<Result.Success<Unit>>(
            service.stop()
        )

        assertEquals(
            listOf(
                "prepare:${asset.id.value}",
                "play",
                "seek:12345",
                "pause",
                "stop"
            ),
            backend.commands
        )

        assertEquals(
            PlaybackStatus.Idle,
            service.state.value.status
        )
        assertEquals(
            null,
            service.state.value.mediaAssetId
        )
    }

    @Test
    fun operationsBeforeInitializeDoNotReachBackend() {
        val backend =
            RecordingBackend()

        val service =
            service(
                backend = backend
            )

        val result =
            assertIs<Result.Failure>(
                service.play()
            )

        assertEquals(
            ErrorCode.INVALID_STATE,
            result.error.code
        )
        assertTrue(
            backend.commands.isEmpty()
        )
        assertEquals(
            PlaybackStatus.Idle,
            service.state.value.status
        )
    }

    @Test
    fun resolverFailureDoesNotReachBackend() {
        val backend =
            RecordingBackend()

        val service =
            DefaultAudioService(
                resourceResolver =
                    FailingResolver(),
                playerBackend =
                    backend
            )

        service.initialize()

        val result =
            assertIs<Result.Failure>(
                service.load(
                    mediaAsset(
                        id = 466L,
                        path =
                            "media/audio/melodies/media-000466.mp4"
                    )
                )
            )

        assertEquals(
            ErrorCode.RESOURCE_UNAVAILABLE,
            result.error.code
        )
        assertTrue(
            backend.commands.isEmpty()
        )
        assertEquals(
            RuntimeState.Ready,
            service.runtimeState
        )
        assertEquals(
            PlaybackStatus.Error,
            service.state.value.status
        )
    }

    @Test
    fun backendPrepareFailureProducesPlaybackError() {
        val backend =
            RecordingBackend(
                failOn =
                    "prepare"
            )

        val service =
            service(
                backend = backend
            )

        service.initialize()

        val result =
            assertIs<Result.Failure>(
                service.load(
                    mediaAsset(
                        id = 7L
                    )
                )
            )

        assertEquals(
            ErrorCode.AUDIO_ERROR,
            result.error.code
        )
        assertEquals(
            listOf(
                "prepare:7"
            ),
            backend.commands
        )
        assertEquals(
            RuntimeState.Ready,
            service.runtimeState
        )
        assertEquals(
            PlaybackStatus.Error,
            service.state.value.status
        )
        assertEquals(
            MediaAssetId(7L),
            service.state.value.mediaAssetId
        )
    }

    @Test
    fun backendPlayFailureProducesPlaybackErrorWithoutKillingService() {
        val backend =
            RecordingBackend(
                failOn =
                    "play"
            )

        val service =
            service(
                backend = backend
            )

        service.initialize()

        assertIs<Result.Success<Unit>>(
            service.load(
                mediaAsset(
                    id = 10L
                )
            )
        )

        val result =
            assertIs<Result.Failure>(
                service.play()
            )

        assertEquals(
            ErrorCode.AUDIO_ERROR,
            result.error.code
        )
        assertEquals(
            RuntimeState.Ready,
            service.runtimeState
        )
        assertEquals(
            PlaybackStatus.Error,
            service.state.value.status
        )
        assertEquals(
            MediaAssetId(10L),
            service.state.value.mediaAssetId
        )
    }

    @Test
    fun negativeSeekIsRejectedBeforeBackendInvocation() {
        val backend =
            RecordingBackend()

        val service =
            service(
                backend = backend
            )

        service.initialize()

        assertIs<Result.Success<Unit>>(
            service.load(
                mediaAsset(
                    id = 11L
                )
            )
        )

        val before =
            service.state.value

        val result =
            assertIs<Result.Failure>(
                service.seekTo(-1L)
            )

        assertEquals(
            ErrorCode.INVALID_ARGUMENT,
            result.error.code
        )
        assertEquals(
            before,
            service.state.value
        )
        assertEquals(
            listOf(
                "prepare:11"
            ),
            backend.commands
        )
    }

    private fun service(
        backend: AudioPlayerBackend
    ): DefaultAudioService {
        return DefaultAudioService(
            resourceResolver =
                SuccessfulResolver(),
            playerBackend =
                backend
        )
    }

    private fun mediaAsset(
        id: Long,
        path: String =
            "media/audio/melodies/media-$id.mp3"
    ): MediaAsset {
        return MediaAsset(
            id =
                MediaAssetId(id),
            type = "AUDIO",
            path = path
        )
    }

    private class SuccessfulResolver :
        MediaResourceResolver {

        override fun resolve(
            mediaAsset: MediaAsset
        ): Result<MediaResource> {
            return Result.Success(
                MediaResource(
                    mediaAssetId =
                        mediaAsset.id,
                    uri =
                        "package://${mediaAsset.path}"
                )
            )
        }
    }

    private class FailingResolver :
        MediaResourceResolver {

        override fun resolve(
            mediaAsset: MediaAsset
        ): Result<MediaResource> {
            return Result.Failure(
                PlatformError(
                    code =
                        ErrorCode.RESOURCE_UNAVAILABLE,
                    message =
                        "Media resource is unavailable."
                )
            )
        }
    }

    private class RecordingBackend(
        private val failOn: String? = null
    ) : AudioPlayerBackend {

        val commands =
            mutableListOf<String>()

        override fun prepare(
            resource: MediaResource
        ): Result<Unit> {
            commands.add(
                "prepare:${resource.mediaAssetId.value}"
            )

            return resultFor(
                "prepare"
            )
        }

        override fun play(): Result<Unit> {
            commands.add(
                "play"
            )

            return resultFor(
                "play"
            )
        }

        override fun pause(): Result<Unit> {
            commands.add(
                "pause"
            )

            return resultFor(
                "pause"
            )
        }

        override fun stop(): Result<Unit> {
            commands.add(
                "stop"
            )

            return resultFor(
                "stop"
            )
        }

        override fun seekTo(
            positionMs: Long
        ): Result<Unit> {
            commands.add(
                "seek:$positionMs"
            )

            return resultFor(
                "seek"
            )
        }

        private fun resultFor(
            command: String
        ): Result<Unit> {
            if (
                failOn ==
                command
            ) {
                return Result.Failure(
                    PlatformError(
                        code =
                            ErrorCode.AUDIO_ERROR,
                        message =
                            "Backend command failed: $command"
                    )
                )
            }

            return Result.Success(Unit)
        }
    }
}
