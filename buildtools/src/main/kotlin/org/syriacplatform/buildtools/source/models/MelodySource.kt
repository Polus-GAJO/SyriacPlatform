package org.syriacplatform.buildtools.source.models

data class MelodySource(
    val id: Long,
    val qoloId: Long?,
    val name: String?,
    val searchName: String?,
    val qintoId: Long?,
    val occasionId: Long?,
    val noteId: Long?,
    val hasRecording: Boolean?
)