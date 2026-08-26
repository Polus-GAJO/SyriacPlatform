package org.syriacplatform.audio.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.syriacplatform.audio.contracts.AudioPlayerBackend
import org.syriacplatform.audio.contracts.MediaResourceResolver
import org.syriacplatform.audio.models.AudioPlayerEvent
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
    fun loadWaitsForBackendReadyEvent() {
        val backend = RecordingBackend()
        val service = service(backend)
        service.initialize()

        assertIs<Result.Success<Unit>>(service.load(mediaAsset(293L)))
        assertEquals(PlaybackStatus.Loading, service.state.value.status)

        backend.emit(AudioPlayerEvent.Ready(45_000L))
        assertEquals(PlaybackStatus.Ready, service.state.value.status)
        assertEquals(45_000L, service.state.value.durationMs)
    }

    @Test
    fun playPauseEndedAndPositionComeFromBackendEvents() {
        val backend = RecordingBackend()
        val service = service(backend)
        service.initialize()

        assertIs<Result.Success<Unit>>(service.load(mediaAsset(10L)))
        backend.emit(AudioPlayerEvent.Ready(100_000L))

        assertIs<Result.Success<Unit>>(service.play())
        assertEquals(PlaybackStatus.Ready, service.state.value.status)

        backend.emit(AudioPlayerEvent.Playing)
        assertEquals(PlaybackStatus.Playing, service.state.value.status)

        backend.emit(AudioPlayerEvent.PositionChanged(12_345L))
        assertEquals(12_345L, service.state.value.positionMs)

        assertIs<Result.Success<Unit>>(service.pause())
        backend.emit(AudioPlayerEvent.Paused)
        assertEquals(PlaybackStatus.Paused, service.state.value.status)

        backend.emit(AudioPlayerEvent.Ended)
        assertEquals(PlaybackStatus.Ended, service.state.value.status)
    }

    @Test
    fun asyncBackendErrorPreservesServiceLifecycle() {
        val backend = RecordingBackend()
        val service = service(backend)
        service.initialize()
        assertIs<Result.Success<Unit>>(service.load(mediaAsset(12L)))

        backend.emit(
            AudioPlayerEvent.Error(
                PlatformError(
                    code = ErrorCode.AUDIO_ERROR,
                    message = "Native player failed."
                )
            )
        )

        assertEquals(RuntimeState.Ready, service.runtimeState)
        assertEquals(PlaybackStatus.Error, service.state.value.status)
        assertEquals(MediaAssetId(12L), service.state.value.mediaAssetId)
    }

    @Test
    fun commandsStillDelegateInOrder() {
        val backend = RecordingBackend()
        val service = service(backend)
        service.initialize()

        assertIs<Result.Success<Unit>>(service.load(mediaAsset(20L)))
        backend.emit(AudioPlayerEvent.Ready(50_000L))
        assertIs<Result.Success<Unit>>(service.play())
        backend.emit(AudioPlayerEvent.Playing)
        assertIs<Result.Success<Unit>>(service.seekTo(12_345L))
        assertIs<Result.Success<Unit>>(service.pause())
        backend.emit(AudioPlayerEvent.Paused)
        assertIs<Result.Success<Unit>>(service.stop())

        assertEquals(
            listOf("prepare:20","play","seek:12345","pause","stop"),
            backend.commands
        )
        assertEquals(PlaybackStatus.Idle, service.state.value.status)
    }

    @Test
    fun operationsBeforeInitializeDoNotReachBackend() {
        val backend = RecordingBackend()
        val service = service(backend)

        val result = assertIs<Result.Failure>(service.play())
        assertEquals(ErrorCode.INVALID_STATE, result.error.code)
        assertTrue(backend.commands.isEmpty())
    }

    @Test
    fun resolverFailureDoesNotReachBackend() {
        val backend = RecordingBackend()
        val service = DefaultAudioService(FailingResolver(), backend)
        service.initialize()

        val result = assertIs<Result.Failure>(
            service.load(
                mediaAsset(
                    466L,
                    "media/audio/melodies/media-000466.mp4"
                )
            )
        )

        assertEquals(ErrorCode.RESOURCE_UNAVAILABLE, result.error.code)
        assertTrue(backend.commands.isEmpty())
        assertEquals(PlaybackStatus.Error, service.state.value.status)
    }

    @Test
    fun prepareFailureIsImmediatePlaybackError() {
        val backend = RecordingBackend(failOn = "prepare")
        val service = service(backend)
        service.initialize()

        val result = assertIs<Result.Failure>(service.load(mediaAsset(7L)))
        assertEquals(ErrorCode.AUDIO_ERROR, result.error.code)
        assertEquals(PlaybackStatus.Error, service.state.value.status)
    }

    @Test
    fun negativeSeekIsRejectedBeforeBackend() {
        val backend = RecordingBackend()
        val service = service(backend)
        service.initialize()
        assertIs<Result.Success<Unit>>(service.load(mediaAsset(21L)))
        backend.emit(AudioPlayerEvent.Ready(60_000L))

        val before = service.state.value
        val result = assertIs<Result.Failure>(service.seekTo(-1L))

        assertEquals(ErrorCode.INVALID_ARGUMENT, result.error.code)
        assertEquals(before, service.state.value)
        assertEquals(listOf("prepare:21"), backend.commands)
    }

    @Test
    fun repeatedPlayWhilePlayingIsSuccessfulNoOp() {
        val backend =
            RecordingBackend()

        val service =
            service(backend)

        service.initialize()

        assertIs<Result.Success<Unit>>(
            service.load(
                mediaAsset(30L)
            )
        )

        backend.emit(
            AudioPlayerEvent.Ready(
                30_000L
            )
        )

        assertIs<Result.Success<Unit>>(
            service.play()
        )

        backend.emit(
            AudioPlayerEvent.Playing
        )

        val commandsBeforeRepeatedPlay =
            backend.commands.toList()

        assertIs<Result.Success<Unit>>(
            service.play()
        )

        assertEquals(
            commandsBeforeRepeatedPlay,
            backend.commands
        )
        assertEquals(
            PlaybackStatus.Playing,
            service.state.value.status
        )
    }

    @Test
    fun repeatedPauseWhilePausedIsSuccessfulNoOp() {
        val backend =
            RecordingBackend()

        val service =
            service(backend)

        service.initialize()

        assertIs<Result.Success<Unit>>(
            service.load(
                mediaAsset(31L)
            )
        )

        backend.emit(
            AudioPlayerEvent.Ready(
                30_000L
            )
        )

        assertIs<Result.Success<Unit>>(
            service.play()
        )

        backend.emit(
            AudioPlayerEvent.Playing
        )

        assertIs<Result.Success<Unit>>(
            service.pause()
        )

        backend.emit(
            AudioPlayerEvent.Paused
        )

        val commandsBeforeRepeatedPause =
            backend.commands.toList()

        assertIs<Result.Success<Unit>>(
            service.pause()
        )

        assertEquals(
            commandsBeforeRepeatedPause,
            backend.commands
        )
        assertEquals(
            PlaybackStatus.Paused,
            service.state.value.status
        )
    }

    @Test
    fun repeatedStopWhileIdleIsSuccessfulNoOp() {
        val backend =
            RecordingBackend()

        val service =
            service(backend)

        service.initialize()

        assertIs<Result.Success<Unit>>(
            service.stop()
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
    fun playAfterEndedSeeksToStartThenReplays() {
        val backend =
            RecordingBackend()

        val service =
            service(backend)

        service.initialize()

        assertIs<Result.Success<Unit>>(
            service.load(
                mediaAsset(32L)
            )
        )

        backend.emit(
            AudioPlayerEvent.Ready(
                30_000L
            )
        )

        assertIs<Result.Success<Unit>>(
            service.play()
        )

        backend.emit(
            AudioPlayerEvent.Playing
        )

        backend.emit(
            AudioPlayerEvent.PositionChanged(
                30_000L
            )
        )

        backend.emit(
            AudioPlayerEvent.Ended
        )

        assertEquals(
            PlaybackStatus.Ended,
            service.state.value.status
        )

        assertIs<Result.Success<Unit>>(
            service.play()
        )

        assertEquals(
            0L,
            service.state.value.positionMs
        )

        assertEquals(
            listOf(
                "prepare:32",
                "play",
                "seek:0",
                "play"
            ),
            backend.commands
        )

        backend.emit(
            AudioPlayerEvent.Playing
        )

        assertEquals(
            PlaybackStatus.Playing,
            service.state.value.status
        )
    }

    @Test
    fun replaySeekFailureProducesPlaybackErrorAndDoesNotPlay() {
        val backend =
            RecordingBackend(
                failOn =
                    "seek"
            )

        val service =
            service(backend)

        service.initialize()

        assertIs<Result.Success<Unit>>(
            service.load(
                mediaAsset(33L)
            )
        )

        backend.emit(
            AudioPlayerEvent.Ready(
                30_000L
            )
        )

        assertIs<Result.Success<Unit>>(
            service.play()
        )

        backend.emit(
            AudioPlayerEvent.Playing
        )

        backend.emit(
            AudioPlayerEvent.Ended
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
            PlaybackStatus.Error,
            service.state.value.status
        )
        assertEquals(
            listOf(
                "prepare:33",
                "play",
                "seek:0"
            ),
            backend.commands
        )
    }
    private fun service(backend: AudioPlayerBackend) =
        DefaultAudioService(SuccessfulResolver(), backend)

    private fun mediaAsset(
        id: Long,
        path: String = "media/audio/melodies/media-$id.mp3"
    ) = MediaAsset(
        id = MediaAssetId(id),
        type = "AUDIO",
        path = path
    )

    private class SuccessfulResolver : MediaResourceResolver {
        override fun resolve(mediaAsset: MediaAsset): Result<MediaResource> =
            Result.Success(
                MediaResource(
                    mediaAssetId = mediaAsset.id,
                    uri = "package://${mediaAsset.path}"
                )
            )
    }

    private class FailingResolver : MediaResourceResolver {
        override fun resolve(mediaAsset: MediaAsset): Result<MediaResource> =
            Result.Failure(
                PlatformError(
                    code = ErrorCode.RESOURCE_UNAVAILABLE,
                    message = "Media resource is unavailable."
                )
            )
    }

    private class RecordingBackend(
        private val failOn: String? = null
    ) : AudioPlayerBackend {
        val commands = mutableListOf<String>()
        private var listener: ((AudioPlayerEvent) -> Unit)? = null

        override fun setEventListener(listener: ((AudioPlayerEvent) -> Unit)?) {
            this.listener = listener
        }

        override fun prepare(resource: MediaResource): Result<Unit> {
            commands += "prepare:${resource.mediaAssetId.value}"
            return resultFor("prepare")
        }

        override fun play(): Result<Unit> {
            commands += "play"
            return resultFor("play")
        }

        override fun pause(): Result<Unit> {
            commands += "pause"
            return resultFor("pause")
        }

        override fun stop(): Result<Unit> {
            commands += "stop"
            return resultFor("stop")
        }

        override fun seekTo(positionMs: Long): Result<Unit> {
            commands += "seek:$positionMs"
            return resultFor("seek")
        }

        fun emit(event: AudioPlayerEvent) {
            listener?.invoke(event)
        }

        private fun resultFor(command: String): Result<Unit> =
            if (failOn == command)
                Result.Failure(
                    PlatformError(
                        code = ErrorCode.AUDIO_ERROR,
                        message = "Backend command failed: $command"
                    )
                )
            else Result.Success(Unit)
    }
}
