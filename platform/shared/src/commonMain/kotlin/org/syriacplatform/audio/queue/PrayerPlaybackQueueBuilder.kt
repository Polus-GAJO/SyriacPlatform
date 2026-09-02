package org.syriacplatform.audio.queue

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.runtime.ResolvedLiturgicalItemTarget
import org.syriacplatform.content.runtime.RuntimePrayerSequence

/**
 * Builds the ordered playable queue for one prayer.
 *
 * Rules:
 * - non-Qolo items are skipped;
 * - effectiveMelody wins when present;
 * - otherwise every valid melodyCandidate is queued in canonical order;
 * - each Melody contributes at most one recording;
 * - a preferred recording is used only when it belongs to that Melody;
 * - otherwise the first canonical recording is used;
 * - a Melody with no recording is skipped.
 */
class PrayerPlaybackQueueBuilder(
    private val contentService: ContentService
) {

    suspend fun build(
        sequence: RuntimePrayerSequence,
        preferredRecordingIds:
            Map<org.syriacplatform.common.types.MelodyId, MediaAssetId> =
            emptyMap()
    ): Result<List<PlaybackQueueEntry>> {
        val entries =
            mutableListOf<PlaybackQueueEntry>()

        for (item in sequence.items) {
            val target =
                item.target as?
                    ResolvedLiturgicalItemTarget.Qolo
                    ?: continue

            val melodies =
                melodiesFor(target)

            for (melody in melodies) {
                when (
                    val recordingsResult =
                        contentService
                            .loadMelodyRecordings(
                                melody.id
                            )
                ) {
                    is Result.Failure ->
                        return recordingsResult

                    is Result.Success -> {
                        val recording =
                            selectRecording(
                                recordings =
                                    recordingsResult.data,
                                preferredRecordingId =
                                    preferredRecordingIds[
                                        melody.id
                                    ]
                            )
                                ?: continue

                        entries +=
                            PlaybackQueueEntry(
                                liturgicalItemId =
                                    item.item.id,
                                melodyId =
                                    melody.id,
                                mediaAsset =
                                    recording
                            )
                    }
                }
            }
        }

        return Result.Success(entries)
    }

    private fun melodiesFor(
        target: ResolvedLiturgicalItemTarget.Qolo
    ): List<Melody> =
        target.effectiveMelody
            ?.let(::listOf)
            ?: target.melodyCandidates

    private fun selectRecording(
        recordings: List<MediaAsset>,
        preferredRecordingId: MediaAssetId?
    ): MediaAsset? {
        if (recordings.isEmpty()) {
            return null
        }

        return preferredRecordingId
            ?.let { preferredId ->
                recordings.firstOrNull {
                    recording ->
                    recording.id == preferredId
                }
            }
            ?: recordings.first()
    }
}
