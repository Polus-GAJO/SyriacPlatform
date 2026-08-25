package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class MelodyJsonDto(
    val id: Long,
    val qoloId: Long,
    val name: String,
    val searchName: String,
    val hasRecording: Boolean,
    val recordingIds: List<Long> = emptyList()
)