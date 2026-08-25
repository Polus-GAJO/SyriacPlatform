package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.packageformat.dto.MediaAssetJsonDto

internal fun MediaAssetJsonDto.toDomain(): Result<MediaAsset> {
    return Result.Success(
        MediaAsset(
            id = MediaAssetId(id),
            type = type,
            path = path
        )
    )
}