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
import org.syriacplatform.content.models.LiturgicalTextRef
import org.syriacplatform.packageformat.dto.LiturgicalItemJsonDto

/**
 * يحول العنصر الليتورجي من البنية الفيزيائية
 * إلى هدف قانوني قوي النوع.
 */
internal fun LiturgicalItemJsonDto.toDomain(): Result<LiturgicalItem> {
    val resolvedTarget =
        when (type.trim().lowercase()) {
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

                if (melodyCandidateIds.isNotEmpty()) {
                    return Result.Failure(
                        PlatformError(
                            code = ErrorCode.INVALID_PACKAGE_DATA,
                            message =
                                "Text liturgical item must not declare " +
                                        "melodyCandidateIds: $id"
                        )
                    )
                }

                if (verses.isNotEmpty()) {
                    return Result.Failure(
                        PlatformError(
                            code = ErrorCode.INVALID_PACKAGE_DATA,
                            message =
                                "Text liturgical item must not declare verses: $id"
                        )
                    )
                }

                LiturgicalItemTarget.Text(
                    textId = TextId(targetId),
                    petgomoId =
                        petgomoId?.let(::PetgomoId)
                )
            }

            "qolo" -> {
                if (petgomoId != null) {
                    return Result.Failure(
                        PlatformError(
                            code = ErrorCode.INVALID_PACKAGE_DATA,
                            message =
                                "Qolo liturgical item must not declare " +
                                        "top-level petgomoId: $id"
                        )
                    )
                }

                LiturgicalItemTarget.Qolo(
                    qoloId = QoloId(targetId),
                    effectiveMelodyId =
                        effectiveMelodyId?.let(::MelodyId),
                    melodyCandidateIds =
                        melodyCandidateIds.map(::MelodyId),
                    verses =
                        verses.map { verse ->
                            LiturgicalTextRef(
                                textId =
                                    TextId(
                                        verse.textId
                                    ),
                                petgomoId =
                                    verse.petgomoId?.let(
                                        ::PetgomoId
                                    )
                            )
                        }
                )
            }

            "qolo-unresolved" -> {
                if (targetId != 0L) {
                    return Result.Failure(
                        PlatformError(
                            code = ErrorCode.INVALID_PACKAGE_DATA,
                            message =
                                "Unresolved Qolo liturgical item must " +
                                        "declare targetId 0: $id"
                        )
                    )
                }

                if (effectiveMelodyId != null) {
                    return Result.Failure(
                        PlatformError(
                            code = ErrorCode.INVALID_PACKAGE_DATA,
                            message =
                                "Unresolved Qolo liturgical item must not " +
                                        "declare effectiveMelodyId: $id"
                        )
                    )
                }

                if (melodyCandidateIds.isNotEmpty()) {
                    return Result.Failure(
                        PlatformError(
                            code = ErrorCode.INVALID_PACKAGE_DATA,
                            message =
                                "Unresolved Qolo liturgical item must not " +
                                        "declare melodyCandidateIds: $id"
                        )
                    )
                }

                if (petgomoId != null) {
                    return Result.Failure(
                        PlatformError(
                            code = ErrorCode.INVALID_PACKAGE_DATA,
                            message =
                                "Unresolved Qolo liturgical item must not " +
                                        "declare top-level petgomoId: $id"
                        )
                    )
                }

                LiturgicalItemTarget.UnresolvedQolo(
                    verses =
                        verses.map { verse ->
                            LiturgicalTextRef(
                                textId =
                                    TextId(
                                        verse.textId
                                    ),
                                petgomoId =
                                    verse.petgomoId?.let(
                                        ::PetgomoId
                                    )
                            )
                        }
                )
            }

            else -> {
                return Result.Failure(
                    PlatformError(
                        code = ErrorCode.UNEXPECTED_ENTITY_TYPE,
                        message =
                            "Unsupported liturgical item type: $type"
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