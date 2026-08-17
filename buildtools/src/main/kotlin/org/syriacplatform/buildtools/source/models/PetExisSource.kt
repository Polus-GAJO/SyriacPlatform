package org.syriacplatform.buildtools.source.models

data class PetExisSource(
    val id: Long,
    val petgomoId: Long?,
    val textId: Long?,
    val existsInTextId: Long?
)