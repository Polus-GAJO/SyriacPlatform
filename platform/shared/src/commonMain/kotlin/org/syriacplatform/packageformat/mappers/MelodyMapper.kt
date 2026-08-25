package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Melody
import org.syriacplatform.packageformat.dto.MelodyJsonDto

internal fun MelodyJsonDto.toDomain(): Result<Melody> {
    return Result.Success(
        Melody(
            id = MelodyId(id),
            qoloId = QoloId(qoloId),
            name = name,
            searchName = searchName,
            hasRecording = hasRecording,
            recordingIds = recordingIds.map(::MediaAssetId)
        )
    )
}