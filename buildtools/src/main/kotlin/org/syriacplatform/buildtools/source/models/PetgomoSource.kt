package org.syriacplatform.buildtools.source.models

data class PetgomoSource(
    val id: Long,
    val abcd: Long?,
    val syriac: String?,
    val searchText: String?
)