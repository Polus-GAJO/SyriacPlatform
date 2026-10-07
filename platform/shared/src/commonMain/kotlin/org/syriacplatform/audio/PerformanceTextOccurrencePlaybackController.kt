package org.syriacplatform.audio

import org.syriacplatform.audio.contracts.AudioService
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PerformanceMediaId
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.TextOccurrenceId
import org.syriacplatform.content.contracts.ContentService

/**
 * Resolves and plays one timed TextOccurrence within the PERFORMANCE
 * recording explicitly selected by the caller.
 *
 * Cardinality is intentional:
 * - zero playable intervals means there is nothing to play;
 * - exactly one interval is played;
 * - more than one interval is ambiguous package/runtime data.
 */
class PerformanceTextOccurrencePlaybackController(
    private val contentService: ContentService,
    private val audioService: AudioService
) {

    suspend fun play(
        performanceId: PerformanceMediaId,
        textOccurrenceId: TextOccurrenceId
    ): Result<Unit> {
        val intervals =
            when (
                val result =
                    contentService
                        .loadPerformanceTextOccurrenceIntervals(
                            performanceId = performanceId,
                            textOccurrenceId = textOccurrenceId
                        )
            ) {
                is Result.Success -> result.data
                is Result.Failure -> return result
            }

        return when (intervals.size) {
            0 ->
                Result.Success(Unit)

            1 -> {
                val interval = intervals.single()

                audioService.playInterval(
                    mediaAsset = interval.mediaAsset,
                    startMs = interval.startMs,
                    endMs = interval.endMs
                )
            }

            else ->
                Result.Failure(
                    PlatformError(
                        code = ErrorCode.INVALID_PACKAGE_DATA,
                        message =
                            "Multiple playable intervals found for " +
                                "the selected PERFORMANCE and TextOccurrence."
                    )
                )
        }
    }
}
