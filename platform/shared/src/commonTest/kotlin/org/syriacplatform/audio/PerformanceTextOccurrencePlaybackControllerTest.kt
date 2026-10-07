package org.syriacplatform.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.syriacplatform.audio.contracts.AudioService
import org.syriacplatform.audio.models.PlaybackState
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MediaSegmentId
import org.syriacplatform.common.types.MediaTimingSetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PerformanceMediaId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.common.types.TextOccurrenceId
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.MediaSegment
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.PerformanceMedia
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.runtime.ResolvedLiturgicalItem
import org.syriacplatform.content.runtime.ResolvedPerformanceMedia
import org.syriacplatform.content.runtime.ResolvedPerformanceTextOccurrenceInterval
import org.syriacplatform.content.runtime.RuntimeEntryPoint
import org.syriacplatform.content.runtime.RuntimeOccasion
import org.syriacplatform.kernel.ServiceMetadata

class PerformanceTextOccurrencePlaybackControllerTest {

    @Test
    fun noPlayableIntervalDoesNotStartAudio() = runTest {
        val content = FakeIntervalContentService(emptyList())
        val audio = FakeIntervalAudioService()
        val controller =
            PerformanceTextOccurrencePlaybackController(content, audio)

        assertIs<Result.Success<Unit>>(
            controller.play(
                PerformanceMediaId(10L),
                TextOccurrenceId(20L)
            )
        )
        assertEquals(emptyList(), audio.commands)
    }

    @Test
    fun exactlyOnePlayableIntervalIsPlayed() = runTest {
        val content =
            FakeIntervalContentService(
                listOf(interval(segmentId = 30L, startMs = 1000L, endMs = 2500L))
            )
        val audio = FakeIntervalAudioService()
        val controller =
            PerformanceTextOccurrencePlaybackController(content, audio)

        assertIs<Result.Success<Unit>>(
            controller.play(
                PerformanceMediaId(10L),
                TextOccurrenceId(20L)
            )
        )
        assertEquals(
            listOf("playInterval:300:1000:2500"),
            audio.commands
        )
    }

    @Test
    fun multiplePlayableIntervalsAreRejectedWithoutAudio() = runTest {
        val content =
            FakeIntervalContentService(
                listOf(
                    interval(segmentId = 30L, startMs = 1000L, endMs = 2500L),
                    interval(segmentId = 31L, startMs = 3000L, endMs = 4000L)
                )
            )
        val audio = FakeIntervalAudioService()
        val controller =
            PerformanceTextOccurrencePlaybackController(content, audio)

        val result =
            controller.play(
                PerformanceMediaId(10L),
                TextOccurrenceId(20L)
            )

        val failure = assertIs<Result.Failure>(result)
        assertEquals(
            ErrorCode.INVALID_PACKAGE_DATA,
            failure.error.code
        )
        assertEquals(emptyList(), audio.commands)
    }

    private fun interval(
        segmentId: Long,
        startMs: Long,
        endMs: Long
    ): ResolvedPerformanceTextOccurrenceInterval =
        ResolvedPerformanceTextOccurrenceInterval(
            performance =
                PerformanceMedia(
                    id = PerformanceMediaId(10L),
                    liturgicalItemId = LiturgicalItemId(100L),
                    mediaAssetId = MediaAssetId(300L),
                    role = "PERFORMANCE",
                    sort = 1L,
                    mediaTimingSetId = MediaTimingSetId(40L)
                ),
            mediaAsset =
                MediaAsset(
                    id = MediaAssetId(300L),
                    type = "AUDIO",
                    path = "media/audio/300.mp3"
                ),
            segment =
                MediaSegment(
                    id = MediaSegmentId(segmentId),
                    mediaTimingSetId = MediaTimingSetId(40L),
                    sequence = 1L,
                    startMs = startMs,
                    endMs = endMs
                ),
            startMs = startMs,
            endMs = endMs
        )
}

private class FakeIntervalContentService(
    private val intervals:
        List<ResolvedPerformanceTextOccurrenceInterval>
) : ContentService {

    override val metadata =
        ServiceMetadata("Fake Interval Content", "1.0")

    override var runtimeState = RuntimeState.Ready
        private set

    override fun initialize() {
        runtimeState = RuntimeState.Ready
    }

    override suspend fun loadQolo(qoloId: QoloId): Result<Qolo> =
        unsupported()

    override suspend fun loadAllQolos(): Result<List<Qolo>> =
        Result.Success(emptyList())

    override suspend fun loadEntryPoints(): Result<List<EntryPoint>> =
        Result.Success(emptyList())

    override suspend fun loadOccasions(): Result<List<Occasion>> =
        Result.Success(emptyList())

    override suspend fun loadDefaultEntryPoint(): Result<RuntimeEntryPoint> =
        unsupported()

    override suspend fun loadOccasion(occasionId: OccasionId): Result<RuntimeOccasion> =
        unsupported()

    override suspend fun loadLiturgicalItem(liturgicalItemId: LiturgicalItemId): Result<ResolvedLiturgicalItem> =
        unsupported()

    override suspend fun loadMelodyRecordings(melodyId: MelodyId): Result<List<MediaAsset>> =
        Result.Success(emptyList())

    override suspend fun loadPerformanceMedia(
        liturgicalItemId: LiturgicalItemId
    ): Result<List<ResolvedPerformanceMedia>> =
        Result.Success(emptyList())

    override suspend fun loadPerformanceTextOccurrenceIntervals(
        performanceId: PerformanceMediaId,
        textOccurrenceId: TextOccurrenceId
    ): Result<List<ResolvedPerformanceTextOccurrenceInterval>> =
        Result.Success(intervals)

    private fun unsupported(): Result.Failure =
        error("Unsupported fake ContentService operation")
}

private class FakeIntervalAudioService : AudioService {

    override val metadata =
        ServiceMetadata("Fake Interval Audio", "1.0")

    override var runtimeState = RuntimeState.Ready
        private set

    private val mutableState = MutableStateFlow(PlaybackState())

    override val state: StateFlow<PlaybackState> = mutableState

    val commands = mutableListOf<String>()

    override fun initialize() {
        runtimeState = RuntimeState.Ready
    }

    override fun shutdown() {
        runtimeState = RuntimeState.NotInitialized
    }

    override fun load(mediaAsset: MediaAsset): Result<Unit> =
        Result.Success(Unit)

    override fun playInterval(
        mediaAsset: MediaAsset,
        startMs: Long,
        endMs: Long
    ): Result<Unit> {
        commands +=
            "playInterval:${mediaAsset.id.value}:$startMs:$endMs"
        return Result.Success(Unit)
    }

    override fun play(): Result<Unit> = Result.Success(Unit)
    override fun pause(): Result<Unit> = Result.Success(Unit)
    override fun stop(): Result<Unit> = Result.Success(Unit)
    override fun seekTo(positionMs: Long): Result<Unit> = Result.Success(Unit)
}
