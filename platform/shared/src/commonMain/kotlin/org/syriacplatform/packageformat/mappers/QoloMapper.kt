package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.GroupId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.packageformat.dto.QoloJsonDto

internal fun QoloJsonDto.toDomain(): Result<Qolo> {
    return Result.Success(
        Qolo(
            id = QoloId(id),
            groupId = GroupId(groupId),
            sort = sort,
            name = name,
            searchName = searchName,
            poeticMeter = poeticMeter
        )
    )
}