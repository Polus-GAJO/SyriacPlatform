package org.syriacplatform.audio.queue

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.syriacplatform.audio.contracts.AudioService
import org.syriacplatform.audio.models.PlaybackState
import org.syriacplatform.audio.models.PlaybackStatus
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.kernel.ServiceMetadata

class PlaybackQueueControllerTest {

    @Test
    fun startLoadsFirstEntryAndPlaysWhenReady() {
        val audio = FakeAudioService()
        val controller = PlaybackQueueController(audio)

        assertIs<Result.Success<Unit>>(
            controller.start(
                listOf(
                    entry(1L, 10L, 100L),
                    entry(2L, 20L, 200L)
                )
            )
        )

        assertEquals(
            listOf("stop", "load:100"),
            audio.commands
        )
        assertEquals(
            0,
            controller.state.value.currentIndex
        )
        assertEquals(
            PlaybackQueueStatus.Loading,
            controller.state.value.status
        )

        audio.emit(
            PlaybackState(
                status = PlaybackStatus.Ready,
                mediaAssetId = MediaAssetId(100L),
                durationMs = 1_000L
            )
        )
        controller.handlePlaybackState(
            audio.state.value
        )

        assertEquals(
            listOf("stop", "load:100", "play"),
            audio.commands
        )
    }

    @Test
    fun endedAdvancesInOrderAndAutoPlaysNextEntry() {
        val audio = FakeAudioService()
        val controller = PlaybackQueueController(audio)

        controller.start(
            listOf(
                entry(1L, 10L, 100L),
                entry(1L, 11L, 110L),
                entry(2L, 20L, 200L)
            )
        )

        readyAndPlay(
            controller,
            audio,
            100L
        )

        audio.emit(
            PlaybackState(
                status = PlaybackStatus.Ended,
                mediaAssetId = MediaAssetId(100L)
            )
        )
        controller.handlePlaybackState(
            audio.state.value
        )

        assertEquals(
            1,
            controller.state.value.currentIndex
        )
        assertEquals(
            MediaAssetId(110L),
            controller.state.value
                .currentEntry
                ?.mediaAsset
                ?.id
        )
        assertEquals(
            "load:110",
            audio.commands.last()
        )

        readyAndPlay(
            controller,
            audio,
            110L
        )

        audio.emit(
            PlaybackState(
                status = PlaybackStatus.Ended,
                mediaAssetId = MediaAssetId(110L)
            )
        )
        controller.handlePlaybackState(
            audio.state.value
        )

        assertEquals(
            2,
            controller.state.value.currentIndex
        )
        assertEquals(
            "load:200",
            audio.commands.last()
        )
    }

    @Test
    fun lastEndedCompletesQueueWithoutReloading() {
        val audio = FakeAudioService()
        val controller = PlaybackQueueController(audio)

        controller.start(
            listOf(
                entry(1L, 10L, 100L)
            )
        )

        readyAndPlay(
            controller,
            audio,
            100L
        )

        val commandsBeforeEnded =
            audio.commands.toList()

        audio.emit(
            PlaybackState(
                status = PlaybackStatus.Ended,
                mediaAssetId = MediaAssetId(100L)
            )
        )
        controller.handlePlaybackState(
            audio.state.value
        )

        assertEquals(
            PlaybackQueueStatus.Completed,
            controller.state.value.status
        )
        assertEquals(
            commandsBeforeEnded,
            audio.commands
        )
    }

    @Test
    fun pauseAndResumeKeepCurrentQueuePosition() {
        val audio = FakeAudioService()
        val controller = PlaybackQueueController(audio)

        controller.start(
            listOf(
                entry(1L, 10L, 100L),
                entry(2L, 20L, 200L)
            )
        )

        readyAndPlay(
            controller,
            audio,
            100L
        )

        audio.emit(
            PlaybackState(
                status = PlaybackStatus.Playing,
                mediaAssetId = MediaAssetId(100L)
            )
        )
        controller.handlePlaybackState(
            audio.state.value
        )

        assertIs<Result.Success<Unit>>(
            controller.pause()
        )
        assertEquals(
            0,
            controller.state.value.currentIndex
        )

        audio.emit(
            PlaybackState(
                status = PlaybackStatus.Paused,
                mediaAssetId = MediaAssetId(100L)
            )
        )
        controller.handlePlaybackState(
            audio.state.value
        )

        assertEquals(
            PlaybackQueueStatus.Paused,
            controller.state.value.status
        )

        assertIs<Result.Success<Unit>>(
            controller.resume()
        )
        assertEquals(
            0,
            controller.state.value.currentIndex
        )
    }

    @Test
    fun stopClearsQueueSession() {
        val audio = FakeAudioService()
        val controller = PlaybackQueueController(audio)

        controller.start(
            listOf(
                entry(1L, 10L, 100L),
                entry(2L, 20L, 200L)
            )
        )

        assertIs<Result.Success<Unit>>(
            controller.stop()
        )

        assertEquals(
            PlaybackQueueState(),
            controller.state.value
        )
        assertEquals(
            "stop",
            audio.commands.last()
        )
    }

    @Test
    fun unrelatedPlaybackStateDoesNotAdvanceQueue() {
        val audio = FakeAudioService()
        val controller = PlaybackQueueController(audio)

        controller.start(
            listOf(
                entry(1L, 10L, 100L),
                entry(2L, 20L, 200L)
            )
        )

        audio.emit(
            PlaybackState(
                status = PlaybackStatus.Ended,
                mediaAssetId = MediaAssetId(999L)
            )
        )
        controller.handlePlaybackState(
            audio.state.value
        )

        assertEquals(
            0,
            controller.state.value.currentIndex
        )
        assertEquals(
            listOf("stop", "load:100"),
            audio.commands
        )
    }

    private fun readyAndPlay(
        controller: PlaybackQueueController,
        audio: FakeAudioService,
        mediaAssetId: Long
    ) {
        audio.emit(
            PlaybackState(
                status = PlaybackStatus.Ready,
                mediaAssetId = MediaAssetId(mediaAssetId),
                durationMs = 1_000L
            )
        )

        controller.handlePlaybackState(
            audio.state.value
        )
    }

    private fun entry(
        liturgicalItemId: Long,
        melodyId: Long,
        mediaAssetId: Long
    ): PlaybackQueueEntry =
        PlaybackQueueEntry(
            liturgicalItemId =
                LiturgicalItemId(
                    liturgicalItemId
                ),
            melodyId =
                MelodyId(
                    melodyId
                ),
            mediaAsset =
                MediaAsset(
                    id =
                        MediaAssetId(
                            mediaAssetId
                        ),
                    type = "AUDIO",
                    path =
                        "media/audio/$mediaAssetId.mp3"
                )
        )
}

private class FakeAudioService :
    AudioService {

    override val metadata =
        ServiceMetadata(
            name = "Fake Audio Service",
            version = "1.0"
        )

    override var runtimeState =
        RuntimeState.Ready
        private set

    private val mutableState =
        MutableStateFlow(
            PlaybackState()
        )

    override val state:
        StateFlow<PlaybackState> =
        mutableState

    val commands =
        mutableListOf<String>()

    override fun initialize() {
        runtimeState =
            RuntimeState.Ready
    }

    override fun shutdown() {
        runtimeState =
            RuntimeState.NotInitialized
    }

    override fun load(
        mediaAsset: MediaAsset
    ): Result<Unit> {
        commands +=
            "load:${mediaAsset.id.value}"

        mutableState.value =
            PlaybackState(
                status =
                    PlaybackStatus.Loading,
                mediaAssetId =
                    mediaAsset.id
            )

        return Result.Success(Unit)
    }

    override fun play(): Result<Unit> {
        commands += "play"
        return Result.Success(Unit)
    }

    override fun pause(): Result<Unit> {
        commands += "pause"
        return Result.Success(Unit)
    }

    override fun stop(): Result<Unit> {
        commands += "stop"

        mutableState.value =
            PlaybackState()

        return Result.Success(Unit)
    }

    override fun seekTo(
        positionMs: Long
    ): Result<Unit> {
        commands +=
            "seek:$positionMs"

        return Result.Success(Unit)
    }

    fun emit(
        playbackState: PlaybackState
    ) {
        mutableState.value =
            playbackState
    }
}
