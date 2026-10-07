package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.MediaSegmentId
import org.syriacplatform.common.types.TextOccurrenceId
import org.syriacplatform.common.types.TextOccurrenceMediaSegmentId
import org.syriacplatform.content.models.TextOccurrenceMediaSegment
import org.syriacplatform.packageformat.dto.TextOccurrenceMediaSegmentJsonDto

internal fun TextOccurrenceMediaSegmentJsonDto.toDomain():
        Result<TextOccurrenceMediaSegment> =
    Result.Success(
        TextOccurrenceMediaSegment(
            id = TextOccurrenceMediaSegmentId(id),
            textOccurrenceId = TextOccurrenceId(textOccurrenceId),
            mediaSegmentId = MediaSegmentId(mediaSegmentId)
        )
    )
