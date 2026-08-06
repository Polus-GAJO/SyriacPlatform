package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QintoId
import org.syriacplatform.content.models.Qinto
import org.syriacplatform.packageformat.dto.QintoJsonDto

internal fun QintoJsonDto.toDomain(): Result<Qinto> {
    return Result.Success(
        Qinto(
            id = QintoId(id),
            name = name
        )
    )
}