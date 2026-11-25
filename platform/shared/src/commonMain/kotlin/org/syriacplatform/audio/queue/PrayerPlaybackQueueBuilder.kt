package org.syriacplatform.audio.queue

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.runtime.RuntimePrayerSequence

/**
 * Builds a prayer queue from recordings of liturgical performances rather than melodies.
 * One item contributes its first contextual performance recording, if available.
 */
class PrayerPlaybackQueueBuilder(private val contentService: ContentService) {
    suspend fun build(sequence: RuntimePrayerSequence): Result<List<PlaybackQueueEntry>> {
        return buildItems(sequence.items.map { it.item.id })
    }

    suspend fun buildItems(itemIds: List<LiturgicalItemId>): Result<List<PlaybackQueueEntry>> {
        val entries = mutableListOf<PlaybackQueueEntry>()
        for (id in itemIds) {
            when (val result = contentService.loadPerformanceMedia(id)) {
                is Result.Failure -> return result
                is Result.Success -> {
                    val recording = result.data.firstOrNull() ?: continue
                    entries += PlaybackQueueEntry(id, recording.mediaAsset)
                }
            }
        }
        return Result.Success(entries)
    }
}
