package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.content.models.Note
import org.syriacplatform.packageformat.dto.NoteJsonDto

internal fun NoteJsonDto.toDomain(): Result<Note> {
    return Result.Success(
        Note(
            id = id,
            text = text
        )
    )
}