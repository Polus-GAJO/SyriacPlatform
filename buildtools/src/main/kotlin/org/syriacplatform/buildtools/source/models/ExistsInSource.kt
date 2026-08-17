package org.syriacplatform.buildtools.source.models

data class ExistsInSource(
    val id: Long,
    val sort: Long?,
    val bookId: Long?,
    val prayerId: Long?,
    val locationId: Long?,
    val qoloId: Long?,
    val qintoId: Long?,
    val noteId: Long?,
    val dayId: Long?
)