package org.syriacplatform.buildtools.source.models

data class NoteSource(
    val id: Long,
    val text: String?,
    val publicationStatus: String
)