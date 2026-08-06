package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.PetgomoId
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.packageformat.dto.LiturgicalItemJsonDto

/**
 * يحول العنصر الليتورجي من البنية الفيزيائية
 * إلى هدف قانوني قوي النوع.
 */
internal fun LiturgicalItemJsonDto.toDomain(): Result<LiturgicalItem> {
    val resolvedTarget = when (type.trim().lowercase()) {
        "text" -> {
            if (effectiveMelodyId != null) {
                return Result.Failure(
                    PlatformError(
                        code = ErrorCode.INVALID_PACKAGE_DATA,
                        message =
                            "Text liturgical item must not declare " +
                                    "effectiveMelodyId: $id"
                    )
                )
            }

            LiturgicalItemTarget.Text(
                textId = TextId(targetId),
                petgomoId = petgomoId?.let(::PetgomoId)
            )
        }

        "qolo" -> {
            if (petgomoId != null) {
                return Result.Failure(
                    PlatformError(
                        code = ErrorCode.INVALID_PACKAGE_DATA,
                        message =
                            "Qolo liturgical item must not declare " +
                                    "petgomoId: $id"
                    )
                )
            }

            val melodyId = effectiveMelodyId
                ?: return Result.Failure(
                    PlatformError(
                        code = ErrorCode.MISSING_REQUIRED_FIELD,
                        message =
                            "Qolo liturgical item requires " +
                                    "effectiveMelodyId: $id"
                    )
                )

            LiturgicalItemTarget.Qolo(
                qoloId = QoloId(targetId),
                effectiveMelodyId = MelodyId(melodyId)
            )
        }

        else -> {
            return Result.Failure(
                PlatformError(
                    code = ErrorCode.UNEXPECTED_ENTITY_TYPE,
                    message = "Unsupported liturgical item type: $type"
                )
            )
        }
    }

    return Result.Success(
        LiturgicalItem(
            id = LiturgicalItemId(id),
            target = resolvedTarget
        )
    )
}