package org.syriacplatform.buildtools.source.models

data class ExistsInTextSource(
    val id: Long,
    val textId: Long?,
    val existsInId: Long?,
    val sortInPrayer: Int?
)