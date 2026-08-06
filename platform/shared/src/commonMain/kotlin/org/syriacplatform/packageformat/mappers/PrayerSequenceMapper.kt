package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.packageformat.dto.PrayerSequenceJsonDto

/**
 * يحول تحقق الصلاة السياقي من JSON إلى النموذج القانوني.
 */
internal fun PrayerSequenceJsonDto.toDomain(): Result<PrayerSequence> {
    return Result.Success(
        PrayerSequence(
            id = PrayerSequenceId(id),
            prayerId = PrayerId(prayerId),
            liturgicalItemIds = liturgicalItemIds.map { value ->
                LiturgicalItemId(value)
            }
        )
    )
}