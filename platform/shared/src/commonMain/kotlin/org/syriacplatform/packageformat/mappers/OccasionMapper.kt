package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.packageformat.dto.OccasionJsonDto

/**
 * يحول سجل المناسبة من JSON إلى النموذج القانوني.
 */
internal fun OccasionJsonDto.toDomain(): Result<Occasion> {
    return Result.Success(
        Occasion(
            id = OccasionId(id),
            name = name,
            description = description,
            prayerSequenceIds = prayerSequenceIds.map { value ->
                PrayerSequenceId(value)
            }
        )
    )
}