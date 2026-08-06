package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * البنية الفيزيائية لسجل داخل occasions.json.
 */
@Serializable
internal data class OccasionJsonDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val prayerSequenceIds: List<Long>
)