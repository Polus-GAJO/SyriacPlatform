package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.PetgomoId
import org.syriacplatform.content.models.Petgomo
import org.syriacplatform.content.models.TextTranslation
import org.syriacplatform.packageformat.dto.PetgomoJsonDto

/**
 * يحول الفتغام من JSON إلى النموذج القانوني.
 */
internal fun PetgomoJsonDto.toDomain(): Result<Petgomo> {
    return Result.Success(
        Petgomo(
            id = PetgomoId(id),
            syriac = syriac,
            translations = translations.map { translation ->
                TextTranslation(
                    language = translation.language,
                    content = translation.content
                )
            }
        )
    )
}