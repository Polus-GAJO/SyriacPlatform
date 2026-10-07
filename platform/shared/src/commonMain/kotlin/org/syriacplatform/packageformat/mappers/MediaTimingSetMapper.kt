package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MediaTimingSetId
import org.syriacplatform.content.models.MediaTimingSet
import org.syriacplatform.packageformat.dto.MediaTimingSetJsonDto

internal fun MediaTimingSetJsonDto.toDomain(): Result<MediaTimingSet> =
    Result.Success(
        MediaTimingSet(
            id = MediaTimingSetId(id),
            mediaAssetId = MediaAssetId(mediaAssetId),
            name = name
        )
    )
