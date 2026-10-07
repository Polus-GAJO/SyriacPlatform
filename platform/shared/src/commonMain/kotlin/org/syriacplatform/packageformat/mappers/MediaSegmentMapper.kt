package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.MediaSegmentId
import org.syriacplatform.common.types.MediaTimingSetId
import org.syriacplatform.content.models.MediaSegment
import org.syriacplatform.packageformat.dto.MediaSegmentJsonDto

internal fun MediaSegmentJsonDto.toDomain(): Result<MediaSegment> =
    Result.Success(
        MediaSegment(
            id = MediaSegmentId(id),
            mediaTimingSetId = MediaTimingSetId(mediaTimingSetId),
            sequence = sequence,
            startMs = startMs,
            endMs = endMs
        )
    )
