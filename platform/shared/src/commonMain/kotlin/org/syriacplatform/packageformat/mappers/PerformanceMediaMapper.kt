package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MediaTimingSetId
import org.syriacplatform.common.types.PerformanceMediaId
import org.syriacplatform.content.models.PerformanceMedia
import org.syriacplatform.packageformat.dto.PerformanceMediaJsonDto

internal fun PerformanceMediaJsonDto.toDomain(): Result<PerformanceMedia> =
    Result.Success(
        PerformanceMedia(
            id = PerformanceMediaId(id),
            liturgicalItemId = LiturgicalItemId(liturgicalItemId),
            mediaAssetId = MediaAssetId(mediaAssetId),
            role = role,
            sort = sort,
            mediaTimingSetId =
                mediaTimingSetId?.let(::MediaTimingSetId)
        )
    )
