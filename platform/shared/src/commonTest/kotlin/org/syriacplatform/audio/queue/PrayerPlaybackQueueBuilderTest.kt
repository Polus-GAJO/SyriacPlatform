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
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.runtime.ResolvedLiturgicalItem
import org.syriacplatform.content.runtime.ResolvedLiturgicalItemTarget
import org.syriacplatform.content.runtime.RuntimeEntryPoint
import org.syriacplatform.content.runtime.RuntimeOccasion
import org.syriacplatform.content.runtime.RuntimePrayerSequence
import org.syriacplatform.kernel.ServiceMetadata

class PrayerPlaybackQueueBuilderTest {

    @Test
    fun multipleMelodyCandidatesAreAllQueuedInOrder() =
        runTest {
            val melody10 =
                melody(
                    id = 10L,
                    qoloId = 100L
                )
            val melody11 =
                melody(
                    id = 11L,
                    qoloId = 100L
                )
            val melody12 =
                melody(
                    id = 12L,
                    qoloId = 100L
                )

            val content =
                FakeContentService(
                    recordings =
                        mapOf(
                            melody10.id to
                                listOf(
                                    recording(1000L)
                                ),
                            melody11.id to
                                listOf(
                                    recording(1100L)
                                ),
                            melody12.id to
                                listOf(
                                    recording(1200L)
                                )
                        )
                )

            val result =
                PrayerPlaybackQueueBuilder(
                    content
                ).build(
                    sequence(
                        qoloItem(
                            itemId = 1L,
                            qoloId = 100L,
                            effectiveMelody = null,
                            candidates =
                                listOf(
                                    melody10,
                                    melody11,
                                    melody12
                                )
                        )
                    )
                )

            val entries =
                assertIs<
                    Result.Success<
                        List<PlaybackQueueEntry>
                    >
                >(result).data

            assertEquals(
                listOf(
                    MelodyId(10L),
                    MelodyId(11L),
                    MelodyId(12L)
                ),
                entries.map {
                    it.melodyId
                }
            )

            assertEquals(
                listOf(
                    MediaAssetId(1000L),
                    MediaAssetId(1100L),
                    MediaAssetId(1200L)
                ),
                entries.map {
                    it.mediaAsset.id
                }
            )
        }

    @Test
    fun effectiveMelodyIsQueuedWithoutAlsoQueuingCandidates() =
        runTest {
            val effective =
                melody(
                    id = 20L,
                    qoloId = 200L
                )
            val alternate =
                melody(
                    id = 21L,
                    qoloId = 200L
                )

            val content =
                FakeContentService(
                    recordings =
                        mapOf(
                            effective.id to
                                listOf(
                                    recording(2000L)
                                ),
                            alternate.id to
                                listOf(
                                    recording(2100L)
                                )
                        )
                )

            val result =
                PrayerPlaybackQueueBuilder(
                    content
                ).build(
                    sequence(
                        qoloItem(
                            itemId = 2L,
                            qoloId = 200L,
                            effectiveMelody =
                                effective,
                            candidates =
                                listOf(
                                    alternate
                                )
                        )
                    )
                )

            val entries =
                assertIs<
                    Result.Success<
                        List<PlaybackQueueEntry>
                    >
                >(result).data

            assertEquals(
                listOf(
                    MelodyId(20L)
                ),
                entries.map {
                    it.melodyId
                }
            )
        }

    @Test
    fun preferredRecordingIsUsedButOnlyOneRecordingIsQueued() =
        runTest {
            val melody =
                melody(
                    id = 30L,
                    qoloId = 300L
                )

            val content =
                FakeContentService(
                    recordings =
                        mapOf(
                            melody.id to
                                listOf(
                                    recording(3000L),
                                    recording(3001L)
                                )
                        )
                )

            val result =
                PrayerPlaybackQueueBuilder(
                    content
                ).build(
                    sequence(
                        qoloItem(
                            itemId = 3L,
                            qoloId = 300L,
                            effectiveMelody =
                                melody
                        )
                    ),
                    preferredRecordingIds =
                        mapOf(
                            melody.id to
                                MediaAssetId(3001L)
                        )
                )

            val entries =
                assertIs<
                    Result.Success<
                        List<PlaybackQueueEntry>
                    >
                >(result).data

            assertEquals(
                1,
                entries.size
            )
            assertEquals(
                MediaAssetId(3001L),
                entries.single()
                    .mediaAsset.id
            )
        }

    @Test
    fun firstRecordingIsUsedWhenNoPreferenceExists() =
        runTest {
            val melody =
                melody(
                    id = 40L,
                    qoloId = 400L
                )

            val content =
                FakeContentService(
                    recordings =
                        mapOf(
                            melody.id to
                                listOf(
                                    recording(4000L),
                                    recording(4001L)
                                )
                        )
                )

            val result =
                PrayerPlaybackQueueBuilder(
                    content
                ).build(
                    sequence(
                        qoloItem(
                            itemId = 4L,
                            qoloId = 400L,
                            effectiveMelody =
                                melody
                        )
                    )
                )

            val entries =
                assertIs<
                    Result.Success<
                        List<PlaybackQueueEntry>
                    >
                >(result).data

            assertEquals(
                1,
                entries.size
            )
            assertEquals(
                MediaAssetId(4000L),
                entries.single()
                    .mediaAsset.id
            )
        }

    @Test
    fun invalidPreferenceFallsBackToFirstCanonicalRecording() =
        runTest {
            val melody =
                melody(
                    id = 50L,
                    qoloId = 500L
                )

            val content =
                FakeContentService(
                    recordings =
                        mapOf(
                            melody.id to
                                listOf(
                                    recording(5000L),
                                    recording(5001L)
                                )
                        )
                )

            val result =
                PrayerPlaybackQueueBuilder(
                    content
                ).build(
                    sequence(
                        qoloItem(
                            itemId = 5L,
                            qoloId = 500L,
                            effectiveMelody =
                                melody
                        )
                    ),
                    preferredRecordingIds =
                        mapOf(
                            melody.id to
                                MediaAssetId(9999L)
                        )
                )

            val entries =
                assertIs<
                    Result.Success<
                        List<PlaybackQueueEntry>
                    >
                >(result).data

            assertEquals(
                MediaAssetId(5000L),
                entries.single()
                    .mediaAsset.id
            )
        }

    @Test
    fun melodyWithoutRecordingIsSkippedAndPrayerOrderIsPreserved() =
        runTest {
            val first =
                melody(
                    id = 60L,
                    qoloId = 600L
                )
            val silent =
                melody(
                    id = 61L,
                    qoloId = 610L
                )
            val last =
                melody(
                    id = 62L,
                    qoloId = 620L
                )

            val content =
                FakeContentService(
                    recordings =
                        mapOf(
                            first.id to
                                listOf(
                                    recording(6000L)
                                ),
                            silent.id to
                                emptyList(),
                            last.id to
                                listOf(
                                    recording(6200L)
                                )
                        )
                )

            val result =
                PrayerPlaybackQueueBuilder(
                    content
                ).build(
                    sequence(
                        qoloItem(
                            itemId = 6L,
                            qoloId = 600L,
                            effectiveMelody =
                                first
                        ),
                        qoloItem(
                            itemId = 7L,
                            qoloId = 610L,
                            effectiveMelody =
                                silent
                        ),
                        qoloItem(
                            itemId = 8L,
                            qoloId = 620L,
                            effectiveMelody =
                                last
                        )
                    )
                )

            val entries =
                assertIs<
                    Result.Success<
                        List<PlaybackQueueEntry>
                    >
                >(result).data

            assertEquals(
                listOf(
                    LiturgicalItemId(6L),
                    LiturgicalItemId(8L)
                ),
                entries.map {
                    it.liturgicalItemId
                }
            )
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
    private val recordings:
        Map<MelodyId, List<MediaAsset>>
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

    override suspend fun loadOccasion(
        occasionId: OccasionId
    ): Result<RuntimeOccasion> =
        unsupported()

    override suspend fun loadLiturgicalItem(
        liturgicalItemId: LiturgicalItemId
    ): Result<ResolvedLiturgicalItem> =
        unsupported()

    override suspend fun loadMelodyRecordings(
        melodyId: MelodyId
    ): Result<List<MediaAsset>> =
        Result.Success(
            recordings[melodyId]
                ?: emptyList()
        )

    private fun unsupported():
        Result.Failure =
        error(
            "Unsupported fake ContentService operation"
        )
}
