package org.syriacplatform.audio.queue

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.GroupId
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.PerformanceMediaId
import org.syriacplatform.common.types.TextOccurrenceId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Note
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.runtime.ResolvedLiturgicalItem
import org.syriacplatform.content.runtime.ResolvedLiturgicalItemTarget
import org.syriacplatform.content.runtime.ResolvedPerformanceMedia
import org.syriacplatform.content.runtime.ResolvedPerformanceTextOccurrenceInterval
import org.syriacplatform.content.runtime.RuntimeEntryPoint
import org.syriacplatform.content.runtime.RuntimeOccasion
import org.syriacplatform.content.runtime.RuntimePrayerSequence
import org.syriacplatform.kernel.ServiceMetadata

class PrayerPlaybackQueueBuilderTest {

    @Test
    fun firstContextualRecordingPerItemInPrayerOrder() = runTest {
        val content = FakeContentService(
            performances = mapOf(
                LiturgicalItemId(1L) to listOf(recording(101L), recording(102L)),
                LiturgicalItemId(2L) to listOf(recording(201L))
            )
        )
        val result = PrayerPlaybackQueueBuilder(content).build(
            sequence(
                qoloItem(1L, 100L, null),
                qoloItem(2L, 200L, null),
                qoloItem(3L, 300L, null)
            )
        )
        val entries = assertIs<Result.Success<List<PlaybackQueueEntry>>>(result).data
        assertEquals(listOf(MediaAssetId(101L), MediaAssetId(201L)), entries.map { it.mediaAsset.id })
        assertEquals(listOf(LiturgicalItemId(1L), LiturgicalItemId(2L)), entries.map { it.liturgicalItemId })
    }

    @Test
    fun missingContextualRecordingsProduceEmptyQueue() = runTest {
        val content = FakeContentService(performances = emptyMap())
        val result = PrayerPlaybackQueueBuilder(content).build(
            sequence(qoloItem(1L, 100L, null))
        )
        assertEquals(emptyList(), assertIs<Result.Success<List<PlaybackQueueEntry>>>(result).data)
    }

    private fun sequence(
        vararg items: ResolvedLiturgicalItem
    ): RuntimePrayerSequence =
        RuntimePrayerSequence(
            sequence =
                PrayerSequence(
                    id =
                        PrayerSequenceId(1L),
                    prayerId =
                        PrayerId(1L),
                    liturgicalItemIds =
                        items.map {
                            it.item.id
                        }
                ),
            prayer =
                Prayer(
                    id =
                        PrayerId(1L),
                    name = "Prayer",
                    description = null
                ),
            items =
                items.toList()
        )

    private fun qoloItem(
        itemId: Long,
        qoloId: Long,
        effectiveMelody: Melody?,
        candidates: List<Melody> =
            emptyList()
    ): ResolvedLiturgicalItem {
        val qolo =
            Qolo(
                id = QoloId(qoloId),
                groupId = GroupId(1L),
                sort = qoloId,
                name = "Qolo $qoloId",
                searchName = "qolo$qoloId",
                poeticMeter = null
            )

        return ResolvedLiturgicalItem(
            item =
                LiturgicalItem(
                    id =
                        LiturgicalItemId(
                            itemId
                        ),
                    target =
                        LiturgicalItemTarget.Qolo(
                            qoloId =
                                qolo.id,
                            effectiveMelodyId =
                                effectiveMelody
                                    ?.id,
                            melodyCandidateIds =
                                candidates.map {
                                    it.id
                                },
                            verses =
                                emptyList()
                        )
                ),
            target =
                ResolvedLiturgicalItemTarget.Qolo(
                    qolo = qolo,
                    effectiveMelody =
                        effectiveMelody,
                    melodyCandidates =
                        candidates,
                    verses =
                        emptyList()
                )
        )
    }

    private fun melody(
        id: Long,
        qoloId: Long
    ): Melody =
        Melody(
            id = MelodyId(id),
            qoloId = QoloId(qoloId),
            name = "Melody $id",
            searchName = "melody$id",
            hasRecording = true
        )

    private fun recording(
        id: Long
    ): MediaAsset =
        MediaAsset(
            id = MediaAssetId(id),
            type = "AUDIO",
            path = "media/audio/$id.mp3"
        )
}

private class FakeContentService(
    private val performances: Map<LiturgicalItemId, List<MediaAsset>>
) : ContentService {

    override val metadata =
        ServiceMetadata(
            name = "Fake Content Service",
            version = "1.0"
        )

    override var runtimeState =
        RuntimeState.Ready
        private set

    override fun initialize() {
        runtimeState =
            RuntimeState.Ready
    }

    override suspend fun loadQolo(
        qoloId: QoloId
    ): Result<Qolo> =
        unsupported()

    override suspend fun loadAllQolos():
        Result<List<Qolo>> =
        Result.Success(emptyList())

    override suspend fun loadEntryPoints():
        Result<List<EntryPoint>> =
        Result.Success(emptyList())

    override suspend fun loadOccasions():
        Result<List<Occasion>> =
        Result.Success(emptyList())

    override suspend fun loadDefaultEntryPoint():
        Result<RuntimeEntryPoint> =
        unsupported()

    override suspend fun loadNote(noteId: Long): Result<Note> =
        unsupported()

    override suspend fun loadOccasion(
        occasionId: OccasionId
    ): Result<RuntimeOccasion> =
        unsupported()

    override suspend fun loadLiturgicalItem(
        liturgicalItemId: LiturgicalItemId
    ): Result<ResolvedLiturgicalItem> =
        unsupported()

    override suspend fun loadMelodyNotations(melodyId: MelodyId): Result<List<MediaAsset>> =
        Result.Success(emptyList())

    override suspend fun loadMelodyRecordings(
        melodyId: MelodyId
    ): Result<List<MediaAsset>> =
        Result.Success(
            emptyList()
                ?: emptyList()
        )

    override suspend fun loadPerformanceMedia(
        liturgicalItemId: LiturgicalItemId
    ): Result<List<ResolvedPerformanceMedia>> =
        Result.Success(
            performances[liturgicalItemId].orEmpty().mapIndexed { index, asset ->
                ResolvedPerformanceMedia(
                    performance = org.syriacplatform.content.models.PerformanceMedia(
                        id = PerformanceMediaId(asset.id.value),
                        liturgicalItemId = liturgicalItemId,
                        mediaAssetId = asset.id,
                        role = "PERFORMANCE",
                        sort = index.toLong()
                    ),
                    mediaAsset = asset
                )
            }
        )

    override suspend fun loadPerformanceTextOccurrenceIntervals(
        performanceId: PerformanceMediaId,
        textOccurrenceId: TextOccurrenceId
    ): Result<List<ResolvedPerformanceTextOccurrenceInterval>> =
        Result.Success(emptyList())

    private fun unsupported():
        Result.Failure =
        error(
            "Unsupported fake ContentService operation"
        )
}
