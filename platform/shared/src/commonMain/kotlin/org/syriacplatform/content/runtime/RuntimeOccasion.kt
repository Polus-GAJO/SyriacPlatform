package org.syriacplatform.content.runtime

import org.syriacplatform.content.models.Occasion

/**
 * Occasion بعد حل PrayerSequences المرتبطة بها.
 */
data class RuntimeOccasion(
    val occasion: Occasion,
    val prayerSequences: List<RuntimePrayerSequence>
)