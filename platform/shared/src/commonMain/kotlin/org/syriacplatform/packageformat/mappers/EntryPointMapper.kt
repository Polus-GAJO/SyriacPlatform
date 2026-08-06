package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.EntryPointTarget
import org.syriacplatform.packageformat.dto.EntryPointJsonDto

/**
 * يحول سجل نقطة الدخول من JSON إلى النموذج القانوني.
 */
internal fun EntryPointJsonDto.toDomain(): Result<EntryPoint> {
    val resolvedTarget = when (type.trim().lowercase()) {
        "occasion" -> {
            EntryPointTarget.Occasion(
                occasionId = OccasionId(targetId)
            )
        }

        else -> {
            return Result.Failure(
                PlatformError(
                    code = ErrorCode.UNEXPECTED_ENTITY_TYPE,
                    message = "Unsupported entry point type: $type"
                )
            )
        }
    }

    return Result.Success(
        EntryPoint(
            id = EntryPointId(id),
            name = name,
            target = resolvedTarget,
            isDefault = isDefault
        )
    )
}