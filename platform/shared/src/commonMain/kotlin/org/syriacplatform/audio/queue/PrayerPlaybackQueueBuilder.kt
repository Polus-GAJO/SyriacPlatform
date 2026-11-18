package org.syriacplatform.audio.queue

import org.syriacplatform.common.result.Result
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.runtime.RuntimePrayerSequence

/**
 * Builds a prayer queue from recordings of liturgical performances rather than melodies.
 * One item contributes its first contextual performance recording, if available.
 */
class PrayerPlaybackQueueBuilder(private val contentService: ContentService) {
    suspend fun build(sequence: RuntimePrayerSequence): Result<List<PlaybackQueueEntry>> {
        val entries = mutableListOf<PlaybackQueueEntry>()
        for (item in sequence.items) {
            when (val result = contentService.loadPerformanceMedia(item.item.id)) {
                is Result.Failure -> return result
                is Result.Success -> {
                    val recording = result.data.firstOrNull() ?: continue
                    entries += PlaybackQueueEntry(item.item.id, recording.mediaAsset)
                }
            }
        }
        return Result.Success(entries)
    }
}
