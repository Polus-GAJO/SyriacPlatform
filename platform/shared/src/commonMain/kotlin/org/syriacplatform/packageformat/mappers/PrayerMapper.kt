package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.packageformat.dto.PrayerJsonDto

/**
 * يحول سجل الصلاة من JSON إلى النموذج القانوني.
 */
internal fun PrayerJsonDto.toDomain(): Result<Prayer> {
    return Result.Success(
        Prayer(
            id = PrayerId(id),
            name = name,
            description = description
        )
    )
}