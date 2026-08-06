package org.syriacplatform.packageformat.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * البنية الفيزيائية لسجل داخل entry-points.json.
 */
@Serializable
internal data class EntryPointJsonDto(
    val id: Long,
    val name: String,
    val type: String,
    val targetId: Long,

    @SerialName("default")
    val isDefault: Boolean = false
)