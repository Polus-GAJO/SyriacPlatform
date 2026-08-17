package org.syriacplatform.buildtools.source.models

data class TextSource(
    val id: Long,
    val syriac: String?,
    val chosen: Boolean?,
    val searchText: String?,
    val searchKey: String?,
    val similarityKey: String?
)