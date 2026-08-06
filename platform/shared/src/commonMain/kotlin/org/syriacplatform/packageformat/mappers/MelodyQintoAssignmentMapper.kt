package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.QintoId
import org.syriacplatform.content.models.MelodyQintoAssignment
import org.syriacplatform.content.models.MelodyQintoRole
import org.syriacplatform.packageformat.dto.MelodyQintoAssignmentJsonDto

internal fun MelodyQintoAssignmentJsonDto.toDomain():
        Result<MelodyQintoAssignment> {

    val resolvedRole = when (role?.trim()?.lowercase()) {
        null,
        "" -> null

        "primary" ->
            MelodyQintoRole.PRIMARY

        "substitute" ->
            MelodyQintoRole.SUBSTITUTE

        else -> {
            return Result.Failure(
                PlatformError(
                    code = ErrorCode.INVALID_PACKAGE_DATA,
                    message = "Unsupported melody-qinto role: $role"
                )
            )
        }
    }

    return Result.Success(
        MelodyQintoAssignment(
            melodyId = MelodyId(melodyId),
            qintoId = QintoId(qintoId),
            role = resolvedRole
        )
    )
}