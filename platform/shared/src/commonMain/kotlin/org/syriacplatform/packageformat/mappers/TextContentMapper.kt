package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.TextId
import org.syriacplatform.content.models.TextContent
import org.syriacplatform.content.models.TextTranslation
import org.syriacplatform.packageformat.dto.TextContentJsonDto
import org.syriacplatform.packageformat.dto.TextTranslationJsonDto

/**
 * يحول البيت النصي من JSON إلى النموذج القانوني.
 */
internal fun TextContentJsonDto.toDomain(): Result<TextContent> {
    return Result.Success(
        TextContent(
            id = TextId(id),
            syriac = syriac,
            translations = translations.map { translation ->
                translation.toDomain()
            }
        )
    )
}

private fun TextTranslationJsonDto.toDomain(): TextTranslation {
    return TextTranslation(
        language = language,
        content = content
    )
}