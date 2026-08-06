package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * البنية الفيزيائية لسجل داخل prayer-sequences.json.
 */
@Serializable
internal data class PrayerSequenceJsonDto(
    val id: Long,
    val prayerId: Long,
    val liturgicalItemIds: List<Long>
)