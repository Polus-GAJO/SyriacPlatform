package org.syriacplatform.content.models

import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PrayerSequenceId

/**
 * مناسبة ليتورجية تشير إلى التحققات السياقية
 * للصلوات الموجودة ضمنها.
 */
data class Occasion(
    val id: OccasionId,
    val name: String,
    val description: String?,
    val prayerSequenceIds: List<PrayerSequenceId>
)