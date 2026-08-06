package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * البنية الفيزيائية لسجل داخل prayers.json.
 */
@Serializable
internal data class PrayerJsonDto(
    val id: Long,
    val name: String,
    val description: String? = null
)